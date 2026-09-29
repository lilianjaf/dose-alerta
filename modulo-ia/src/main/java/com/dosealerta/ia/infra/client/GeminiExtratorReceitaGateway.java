package com.dosealerta.ia.infra.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tika.Tika;
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
			Você é um assistente que extrai dados estruturados de fotos de receitas para um sistema de \
			lembretes de medicação.

			Primeiro, decida se a imagem é uma receita formal: um receituário com a prescrição de \
			medicamentos, identificado pelo nome e pelo registro profissional de quem prescreveu \
			(médico com CRM ou dentista com CRO), no cabeçalho, carimbo ou assinatura. Fotos de caixas \
			de remédio, bulas, exames, embalagens, textos soltos ou qualquer outra coisa não são \
			receitas: nesse caso marque receitaMedica como false.

			Extraia nomePrescritor e registroProfissional exatamente como aparecem na imagem (ex: \
			'CRM 12.345', 'CRO/SC 99999'). Se algum deles não estiver legível ou não constar, deixe-o \
			nulo; nunca invente nome nem número de registro. Se receitaMedica for false, deixe os \
			demais campos nulos e a lista de medicamentos vazia.

			Extraia TODOS os medicamentos prescritos, um item da lista para cada um, na ordem em que \
			aparecem. Se dose ou frequência de um medicamento não constarem ou não estiverem legíveis, \
			deixe o campo nulo: nunca estime nem invente valores. Em dose, use a quantidade de cada \
			administração e escreva a unidade por extenso, sem abreviações (ex: '50mg', '2 doses', \
			'2 jatos'; 'cp' vira 'comprimido', 'cap' vira 'cápsula', 'gts' vira 'gotas').

			Em duracaoDias: se a receita informar por quantos dias tomar, use esse número. Se não \
			informar um prazo mas indicar uso contínuo ou repetido diariamente sem data de término \
			(ex: 'uso contínuo', cremes e loções de uso diário), use 30. Só deixe duracaoDias nulo \
			quando não for possível saber se é um tratamento com prazo definido ou de uso contínuo.""";

	private static final Logger LOG = LoggerFactory.getLogger(GeminiExtratorReceitaGateway.class);

	private static final int MAX_TENTATIVAS = 4;

	private static final String INSTRUCAO_USUARIO = "Extraia os dados desta receita médica.";

	private static final Map<String, Object> SCHEMA_MEDICAMENTO = Map.of(
			"type", "OBJECT",
			"properties", Map.of(
					"medicamento", Map.of(
							"type", "STRING",
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
							"description",
									"Duração total do tratamento, em dias, somente se a receita informar; nulo se não constar")),
			"required", List.of("medicamento"));

	private static final Map<String, Object> SCHEMA_RESPOSTA = Map.of(
			"type", "OBJECT",
			"description", "Dados estruturados extraídos de uma foto de receita",
			"properties", Map.of(
					"receitaMedica", Map.of(
							"type", "BOOLEAN",
							"description",
									"true somente se a imagem for uma receita formal com prescrição de medicamentos"),
					"nomePrescritor", Map.of(
							"type", "STRING",
							"nullable", true,
							"description", "Nome do médico ou dentista que prescreveu; nulo se não constar"),
					"registroProfissional", Map.of(
							"type", "STRING",
							"nullable", true,
							"description", "Registro no conselho (CRM ou CRO) do prescritor; nulo se não constar"),
					"medicamentos", Map.of(
							"type", "ARRAY",
							"description", "Todos os medicamentos prescritos, na ordem da receita",
							"items", SCHEMA_MEDICAMENTO)),
			"required", List.of("receitaMedica"));

	private static final Set<String> MOTIVOS_DE_RECUSA =
			Set.of("SAFETY", "PROHIBITED_CONTENT", "BLOCKLIST", "SPII", "IMAGE_SAFETY", "RECITATION");

	private final RestClient geminiRestClient;
	private final List<String> modelos;
	private final double temperatura;
	private final int maxOutputTokens;
	private final long backoffInicialMs;
	private final MeterRegistry meterRegistry;
	private final ObjectMapper objectMapper = new ObjectMapper();

	GeminiExtratorReceitaGateway(
			RestClient geminiRestClient,
			@Value("${ia.modelos:gemini-3.5-flash-lite}") String modelosConfigurados,
			@Value("${ia.gemini.temperature:0.1}") double temperatura,
			@Value("${ia.gemini.max-output-tokens:2048}") int maxOutputTokens,
			@Value("${ia.gemini.retry-backoff-ms:1000}") long backoffInicialMs,
			MeterRegistry meterRegistry) {
		this.geminiRestClient = geminiRestClient;
		this.modelos = Arrays.stream(modelosConfigurados.split(","))
				.map(String::strip)
				.filter(m -> !m.isEmpty())
				.toList();
		if (this.modelos.isEmpty()) {
			throw new IllegalArgumentException("ia.modelos precisa ter ao menos um modelo configurado");
		}
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
			RespostaBruta bruta = chamarComFallbackDeModelos(requisicao);
			resposta = lerResposta(bruta.corpo(), bruta.modelo());
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

	private RespostaBruta chamarComFallbackDeModelos(GenerateContentRequest requisicao) {
		RestClientException ultimaFalha = null;
		for (int i = 0; i < modelos.size(); i++) {
			String modelo = modelos.get(i);
			try {
				return new RespostaBruta(chamarComRetry(requisicao, modelo), modelo);
			} catch (RestClientException e) {
				ultimaFalha = e;
				if (i < modelos.size() - 1) {
					LOG.warn("Modelo {} indisponível, tentando o próximo modelo configurado", modelo);
				}
			}
		}
		throw ultimaFalha;
	}

	private String chamarComRetry(GenerateContentRequest requisicao, String modelo) {
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

	private record RespostaBruta(String corpo, String modelo) {}

	private GenerateContentResponse lerResposta(String corpo, String modelo) {
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

		List<MedicamentoExtraido> medicamentos = extraida.medicamentos() == null
				? List.of()
				: extraida.medicamentos().stream()
						.map(m -> new MedicamentoExtraido(
								m.medicamento(),
								m.dose(),
								m.frequenciaHoras(),
								m.duracaoDias()))
						.toList();

		return new ReceitaExtraida(
				extraida.receitaMedica(), extraida.nomePrescritor(), extraida.registroProfissional(), medicamentos);
	}

	private void registrarLatencia(Timer.Sample amostra, String outcome) {
		amostra.stop(Timer.builder("ia.extracao.latencia")
				.tag("outcome", outcome)
				.description("Latência da chamada ao modelo de visão para extração de receita")
				.register(meterRegistry));
	}

	private static final Set<String> MIME_TYPES_SUPORTADOS = Set.of("image/png", "image/jpeg", "image/webp");

	private static final Tika TIKA = new Tika();

	private String detectarMimeType(byte[] imagem) {
		String mimeType = TIKA.detect(imagem);
		if (!MIME_TYPES_SUPORTADOS.contains(mimeType)) {
			throw new ImagemReceitaInvalidaException("Formato de imagem não suportado. Envie a foto em JPEG, PNG ou WEBP");
		}
		return mimeType;
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
