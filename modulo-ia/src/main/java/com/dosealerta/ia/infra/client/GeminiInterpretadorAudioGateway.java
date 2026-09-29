package com.dosealerta.ia.infra.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.exception.InterpretacaoAudioFalhouException;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
class GeminiInterpretadorAudioGateway implements InterpretadorAudioGateway {

	private static final String PROMPT_SISTEMA =
			"""
			Você é um assistente que interpreta áudios enviados por pacientes no WhatsApp, em resposta a \
			mensagens sobre a medicação: confirmação de uma receita recém-lida, pergunta se o paciente já \
			tomou a dose, ou lembrete de um alarme de medicação.

			Classifique a intenção do áudio em uma destas opções:
			- TOMEI: o paciente diz que já tomou o remédio.
			- NAO_TOMEI: o paciente diz que ainda não tomou.
			- CONFIRMAR: o paciente confirma que os dados lidos da receita estão certos, sem corrigir nada.
			- CORRECAO: o paciente está corrigindo a dose, a frequência ou a duração do tratamento.
			- NAO_ENTENDIDO: o áudio não se encaixa em nenhuma das opções acima, está incompreensível ou \
			fala de outro assunto.

			Se a intenção for CORRECAO, extraia dose, frequenciaHoras e duracaoDias do que for dito; deixe \
			nulo o que não for mencionado.""";

	private static final Logger LOG = LoggerFactory.getLogger(GeminiInterpretadorAudioGateway.class);

	private static final int MAX_TENTATIVAS = 4;

	private static final String MENSAGEM_FALHA_GEMINI = "Serviço de interpretação de áudio indisponível";

	private static final String INSTRUCAO_USUARIO = "Classifique a intenção deste áudio.";

	private static final Map<String, Object> SCHEMA_RESPOSTA = Map.of(
			"type", "OBJECT",
			"properties", Map.of(
					"intencao", Map.of(
							"type", "STRING",
							"enum", List.of("TOMEI", "NAO_TOMEI", "CONFIRMAR", "CORRECAO", "NAO_ENTENDIDO")),
					"dose", Map.of(
							"type", "STRING",
							"nullable", true),
					"frequenciaHoras", Map.of(
							"type", "INTEGER",
							"nullable", true),
					"duracaoDias", Map.of(
							"type", "INTEGER",
							"nullable", true)),
			"required", List.of("intencao"));

	private final RestClient geminiRestClient;
	private final List<String> modelos;
	private final double temperatura;
	private final int maxOutputTokens;
	private final long backoffInicialMs;
	private final ObjectMapper objectMapper = new ObjectMapper();

	GeminiInterpretadorAudioGateway(
			RestClient geminiRestClient,
			@Value("${ia.modelos:gemini-3.5-flash-lite}") String modelosConfigurados,
			@Value("${ia.gemini.temperature:0.1}") double temperatura,
			@Value("${ia.gemini.max-output-tokens:2048}") int maxOutputTokens,
			@Value("${ia.gemini.retry-backoff-ms:1000}") long backoffInicialMs) {
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
	}

	@Override
	public AudioInterpretadoOutput interpretar(byte[] audio, String tipoConteudo) {
		try {
			String corpo = chamarComFallbackDeModelos(requisicao(audio, tipoConteudo));
			return converter(corpo);
		} catch (RestClientException e) {
			LOG.warn("Falha ao interpretar áudio via Gemini: {}", descrever(e));
			throw new InterpretacaoAudioFalhouException(MENSAGEM_FALHA_GEMINI, e);
		}
	}

	private String chamarComFallbackDeModelos(GenerateContentRequest requisicao) {
		RestClientException ultimaFalha = null;
		for (int i = 0; i < modelos.size(); i++) {
			String modelo = modelos.get(i);
			try {
				return chamarComRetry(requisicao, modelo);
			} catch (RestClientException e) {
				ultimaFalha = e;
				if (i < modelos.size() - 1) {
					LOG.warn("Modelo {} indisponível para interpretar áudio, tentando o próximo modelo configurado", modelo);
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
						"Chamada ao Gemini para interpretar áudio falhou (modelo={}, tentativa {}/{}): {}",
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

	private boolean podeTentarNovamente(RestClientException e) {
		if (e instanceof HttpStatusCodeException http) {
			return http.getStatusCode().is5xxServerError();
		}
		return e instanceof ResourceAccessException;
	}

	private String descrever(RestClientException e) {
		if (e instanceof HttpStatusCodeException http) {
			return http.getStatusCode() + " " + truncar(http.getResponseBodyAsString());
		}
		return e.getMessage();
	}

	private String truncar(String texto) {
		return texto.length() > 2000 ? texto.substring(0, 2000) : texto;
	}

	private void aguardarBackoff(int tentativa) {
		try {
			Thread.sleep(backoffInicialMs * (1L << (tentativa - 1)));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interpretação de áudio interrompida", e);
		}
	}

	private GenerateContentRequest requisicao(byte[] audio, String tipoConteudo) {
		return new GenerateContentRequest(
				new Content(null, List.of(Part.texto(PROMPT_SISTEMA))),
				List.of(new Content(
						"user",
						List.of(
								Part.audio(mimeType(tipoConteudo), Base64.getEncoder().encodeToString(audio)),
								Part.texto(INSTRUCAO_USUARIO)))),
				new GenerationConfig("application/json", SCHEMA_RESPOSTA, temperatura, maxOutputTokens));
	}

	private String mimeType(String tipoConteudo) {
		if (tipoConteudo == null) {
			return "audio/ogg";
		}
		int ponto = tipoConteudo.indexOf(';');
		return ponto >= 0 ? tipoConteudo.substring(0, ponto).strip() : tipoConteudo.strip();
	}

	private AudioInterpretadoOutput converter(String corpo) {
		if (corpo == null || corpo.isBlank()) {
			return AudioInterpretadoOutput.naoEntendido();
		}
		try {
			GenerateContentResponse resposta = objectMapper.readValue(corpo, GenerateContentResponse.class);
			String json = extrairJson(resposta);
			if (json == null) {
				return AudioInterpretadoOutput.naoEntendido();
			}
			AudioInterpretadoOutput interpretado = objectMapper.readValue(json, AudioInterpretadoOutput.class);
			return interpretado == null || interpretado.intencao() == null
					? AudioInterpretadoOutput.naoEntendido()
					: interpretado;
		} catch (JacksonException e) {
			LOG.warn("Resposta do Gemini para interpretação de áudio não pôde ser interpretada");
			return AudioInterpretadoOutput.naoEntendido();
		}
	}

	private String extrairJson(GenerateContentResponse resposta) {
		if (resposta == null || resposta.candidates() == null || resposta.candidates().isEmpty()) {
			return null;
		}
		Content content = resposta.candidates().getFirst().content();
		if (content == null || content.parts() == null) {
			return null;
		}
		return content.parts().stream()
				.map(Part::text)
				.filter(texto -> texto != null && !texto.isBlank())
				.findFirst()
				.orElse(null);
	}

	record GenerateContentRequest(Content systemInstruction, List<Content> contents, GenerationConfig generationConfig) {}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record Content(String role, List<Part> parts) {}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record Part(String text, InlineData inlineData) {

		static Part texto(String texto) {
			return new Part(texto, null);
		}

		static Part audio(String mimeType, String base64) {
			return new Part(null, new InlineData(mimeType, base64));
		}
	}

	record InlineData(String mimeType, String data) {}

	record GenerationConfig(
			String responseMimeType, Map<String, Object> responseSchema, double temperature, int maxOutputTokens) {}

	record GenerateContentResponse(List<Candidate> candidates) {}

	record Candidate(Content content) {}
}
