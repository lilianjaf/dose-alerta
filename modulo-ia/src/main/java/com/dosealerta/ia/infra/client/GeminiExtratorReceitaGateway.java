package com.dosealerta.ia.infra.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
class GeminiExtratorReceitaGateway implements ExtratorReceitaGateway {

	private static final String PROMPT_SISTEMA =
			"""
			Você é um assistente que extrai dados estruturados de fotos de receitas médicas \
			para um sistema de lembretes de medicação.

			Primeiro, decida se a imagem é uma receita médica formal: um receituário com a \
			prescrição de um medicamento, identificado pelo nome e pelo CRM do médico \
			(cabeçalho, carimbo ou assinatura). Fotos de caixas de remédio, bulas, exames, \
			embalagens, textos soltos ou qualquer outra coisa não são receitas: nesse caso \
			marque receitaMedica como false.

			Extraia nomeMedico e crm exatamente como aparecem na imagem. Se algum deles não \
			estiver legível ou não constar, deixe-o nulo; nunca invente nome de médico nem \
			número de CRM. Se receitaMedica for false, deixe os demais campos nulos.

			Se a receita prescrever mais de um medicamento, extraia apenas o primeiro. Se um \
			dado do medicamento não estiver legível, faça a melhor estimativa a partir do que \
			está escrito; não invente valores sem nenhuma base na imagem. Em dose, use a \
			quantidade de cada administração como escrita (ex: '50mg', '2 doses', '2 jatos'). \
			Para uso contínuo ou sem duração definida, use 30 em duracaoDias.""";

	private static final Logger LOG = LoggerFactory.getLogger(GeminiExtratorReceitaGateway.class);

	private static final int MAX_TENTATIVAS = 4;

	private static final String INSTRUCAO_USUARIO = "Extraia os dados desta receita médica.";

	private static final Map<String, Object> SCHEMA_RESPOSTA = Map.of(
			"type", "OBJECT",
			"description", "Dados estruturados extraídos de uma foto de receita médica",
			"properties", Map.of(
					"receitaMedica", Map.of(
							"type", "BOOLEAN",
							"description",
									"true somente se a imagem for uma receita médica formal com prescrição de medicamento"),
					"nomeMedico", Map.of(
							"type", "STRING",
							"nullable", true,
							"description", "Nome do médico prescritor, como aparece na receita; nulo se não constar"),
					"crm", Map.of(
							"type", "STRING",
							"nullable", true,
							"description", "Número do CRM do médico, como aparece na receita; nulo se não constar"),
					"medicamento", Map.of(
							"type", "STRING",
							"nullable", true,
							"description", "Nome do medicamento prescrito, exatamente como escrito na receita"),
					"dose", Map.of(
							"type", "STRING",
							"nullable", true,
							"description",
									"Dose de cada administração, incluindo a unidade, ex: '50mg', '1 comprimido', '2 doses'"),
					"frequenciaHoras", Map.of(
							"type", "INTEGER",
							"nullable", true,
							"description",
									"Intervalo entre as doses, em horas, ex: 8 para 'de 8 em 8 horas', 24 para 'uma vez ao dia'"),
					"duracaoDias", Map.of(
							"type", "INTEGER",
							"nullable", true,
							"description", "Duração total do tratamento, em dias, conforme prescrito")),
			"required", List.of("receitaMedica"));

	// finishReason em que a Gemini se recusa a responder por política de segurança/conteúdo.
	private static final Set<String> MOTIVOS_DE_RECUSA =
			Set.of("SAFETY", "PROHIBITED_CONTENT", "BLOCKLIST", "SPII", "IMAGE_SAFETY", "RECITATION");

	private final RestClient geminiRestClient;
	private final String modelo;
	private final double temperatura;
	private final int maxOutputTokens;
	private final long backoffInicialMs;
	private final MeterRegistry meterRegistry;
	private final ObjectMapper objectMapper = new ObjectMapper();

	GeminiExtratorReceitaGateway(
			RestClient geminiRestClient,
			@Value("${ia.modelo:gemini-3.5-flash-lite}") String modelo,
			@Value("${ia.gemini.temperature:0.1}") double temperatura,
			@Value("${ia.gemini.max-output-tokens:2048}") int maxOutputTokens,
			@Value("${ia.gemini.retry-backoff-ms:1000}") long backoffInicialMs,
			MeterRegistry meterRegistry) {
		this.geminiRestClient = geminiRestClient;
		this.modelo = modelo;
		this.temperatura = temperatura;
		this.maxOutputTokens = maxOutputTokens;
		this.backoffInicialMs = backoffInicialMs;
		this.meterRegistry = meterRegistry;
	}

	@Override
	public ReceitaExtraida extrair(byte[] imagem) {
		GenerateContentRequest requisicao = new GenerateContentRequest(
				new Content(null, List.of(Part.texto(PROMPT_SISTEMA))),
				List.of(new Content(
						"user",
						List.of(Part.imagem(detectarMimeType(imagem), Base64.getEncoder().encodeToString(imagem)),
								Part.texto(INSTRUCAO_USUARIO)))),
				new GenerationConfig("application/json", SCHEMA_RESPOSTA, temperatura, maxOutputTokens));

		GenerateContentResponse resposta;
		Timer.Sample amostra = Timer.start(meterRegistry);
		try {
			resposta = lerResposta(chamarComRetry(requisicao));
			registrarLatencia(amostra, "sucesso");
		} catch (RestClientException e) {
			registrarLatencia(amostra, "falha");
			throw new ExtracaoReceitaFalhouException(
					limiteExcedido(e)
							? "Limite de uso do modelo de visão atingido. Tente novamente em alguns instantes"
							: "Falha ao chamar o modelo de visão",
					e);
		}

		return converter(resposta);
	}

	private String chamarComRetry(GenerateContentRequest requisicao) {
		for (int tentativa = 1; ; tentativa++) {
			try {
				return geminiRestClient
						.post()
						.uri("/v1beta/models/{modelo}:generateContent", modelo)
						.body(requisicao)
						.retrieve()
						.body(String.class);
			} catch (RestClientException e) {
				LOG.warn(
						"Chamada ao Gemini falhou (modelo={}, tentativa {}/{}): {}",
						modelo,
						tentativa,
						MAX_TENTATIVAS,
						descrever(e));
				if (tentativa == MAX_TENTATIVAS || !podeTentarNovamente(e)) {
					throw e;
				}
				aguardarBackoff(tentativa);
			}
		}
	}

	// Lê o corpo como texto e parseia aqui: não depende do Content-Type da resposta.
	private GenerateContentResponse lerResposta(String corpo) {
		if (corpo == null || corpo.isBlank()) {
			throw new ExtracaoReceitaFalhouException("O modelo de visão devolveu uma resposta vazia");
		}
		try {
			return objectMapper.readValue(corpo, GenerateContentResponse.class);
		} catch (JacksonException e) {
			LOG.warn("Resposta do Gemini não é um JSON válido (modelo={}): {}", modelo, truncar(corpo));
			throw new ExtracaoReceitaFalhouException("Resposta do modelo de visão não pôde ser interpretada", e);
		}
	}

	private String truncar(String texto) {
		return texto.length() > 2000 ? texto.substring(0, 2000) : texto;
	}

	// 5xx (ex: 503 "high demand") e falha de rede são transitórios. 429 (cota excedida) não é: o Google pede
	// para aguardar dezenas de segundos, e cada nova tentativa ainda consome cota. 4xx também não.
	private boolean podeTentarNovamente(RestClientException e) {
		if (e instanceof HttpStatusCodeException http) {
			return http.getStatusCode().is5xxServerError();
		}
		return e instanceof ResourceAccessException;
	}

	private boolean limiteExcedido(RestClientException e) {
		return e instanceof HttpStatusCodeException http && http.getStatusCode().value() == 429;
	}

	private String descrever(RestClientException e) {
		if (e instanceof HttpStatusCodeException http) {
			return http.getStatusCode() + " " + truncar(http.getResponseBodyAsString());
		}
		return e.getMessage();
	}

	private void aguardarBackoff(int tentativa) {
		try {
			Thread.sleep(backoffInicialMs * (1L << (tentativa - 1)));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExtracaoReceitaFalhouException("Extração da receita interrompida", e);
		}
	}

	private ReceitaExtraida converter(GenerateContentResponse resposta) {
		if (resposta == null) {
			throw new ExtracaoReceitaFalhouException("O modelo de visão devolveu uma resposta vazia");
		}
		if (resposta.promptFeedback() != null && resposta.promptFeedback().blockReason() != null) {
			throw new ExtracaoReceitaFalhouException("O modelo recusou processar a imagem da receita");
		}

		Candidate candidato = resposta.candidates() == null || resposta.candidates().isEmpty()
				? null
				: resposta.candidates().getFirst();
		if (candidato == null) {
			throw new ExtracaoReceitaFalhouException("Resposta da IA não trouxe nenhum candidato");
		}
		if (MOTIVOS_DE_RECUSA.contains(candidato.finishReason())) {
			throw new ExtracaoReceitaFalhouException("O modelo recusou processar a imagem da receita");
		}
		if (candidato.finishReason() != null && !"STOP".equals(candidato.finishReason())) {
			throw new ExtracaoReceitaFalhouException(
					"Resposta da IA foi interrompida (finishReason=" + candidato.finishReason() + ")");
		}

		String json = candidato.content() == null || candidato.content().parts() == null
				? null
				: candidato.content().parts().stream()
						.map(Part::text)
						.filter(texto -> texto != null && !texto.isBlank())
						.findFirst()
						.orElse(null);
		if (json == null) {
			throw new ExtracaoReceitaFalhouException("Resposta da IA não trouxe um bloco de texto estruturado");
		}

		ReceitaExtraidaIA extraida;
		try {
			extraida = objectMapper.readValue(json, ReceitaExtraidaIA.class);
		} catch (JacksonException e) {
			throw new ExtracaoReceitaFalhouException("Resposta da IA não está no formato estruturado esperado", e);
		}

		return new ReceitaExtraida(
				extraida.medicamento(),
				extraida.dose(),
				extraida.frequenciaHoras() == null ? 0 : extraida.frequenciaHoras(),
				extraida.duracaoDias() == null ? 0 : extraida.duracaoDias(),
				extraida.receitaMedica(),
				extraida.nomeMedico(),
				extraida.crm());
	}

	private void registrarLatencia(Timer.Sample amostra, String outcome) {
		amostra.stop(Timer.builder("ia.extracao.latencia")
				.tag("outcome", outcome)
				.description("Latência da chamada ao modelo de visão para extração de receita")
				.register(meterRegistry));
	}

	private String detectarMimeType(byte[] imagem) {
		if (imagem.length >= 8
				&& (imagem[0] & 0xFF) == 0x89
				&& imagem[1] == 'P'
				&& imagem[2] == 'N'
				&& imagem[3] == 'G') {
			return "image/png";
		}
		if (imagem.length >= 3 && (imagem[0] & 0xFF) == 0xFF && (imagem[1] & 0xFF) == 0xD8) {
			return "image/jpeg";
		}
		if (imagem.length >= 12 && imagem[8] == 'W' && imagem[9] == 'E' && imagem[10] == 'B' && imagem[11] == 'P') {
			return "image/webp";
		}

		throw new ImagemReceitaInvalidaException("Formato de imagem não suportado. Envie a foto em JPEG, PNG ou WEBP");
	}

	record GenerateContentRequest(Content systemInstruction, List<Content> contents, GenerationConfig generationConfig) {}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record Content(String role, List<Part> parts) {}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record Part(String text, InlineData inlineData) {

		static Part texto(String texto) {
			return new Part(texto, null);
		}

		static Part imagem(String mimeType, String base64) {
			return new Part(null, new InlineData(mimeType, base64));
		}
	}

	record InlineData(String mimeType, String data) {}

	record GenerationConfig(
			String responseMimeType, Map<String, Object> responseSchema, double temperature, int maxOutputTokens) {}

	record GenerateContentResponse(List<Candidate> candidates, PromptFeedback promptFeedback) {}

	record Candidate(Content content, String finishReason) {}

	record PromptFeedback(String blockReason) {}
}
