package com.dosealerta.ia.infra.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ClaudeExtratorReceitaGateway implements ExtratorReceitaGateway {

	private static final String PROMPT_SISTEMA =
			"""
			Você é um assistente que extrai dados estruturados de fotos de receitas médicas \
			para um sistema de lembretes de medicação. Leia a imagem e preencha exatamente os \
			campos pedidos. Se a receita prescrever mais de um medicamento, extraia apenas o \
			primeiro. Se algum dado não estiver legível ou não constar na receita, faça a \
			melhor estimativa possível a partir do que está escrito; não invente valores \
			sem nenhuma base na imagem.""";

	private static final String INSTRUCAO_USUARIO = "Extraia os dados desta receita médica.";

	private final AnthropicClient client;
	private final String modelo;
	private final MeterRegistry meterRegistry;

	ClaudeExtratorReceitaGateway(
			AnthropicClient client, @Value("${ia.modelo:claude-opus-5}") String modelo, MeterRegistry meterRegistry) {
		this.client = client;
		this.modelo = modelo;
		this.meterRegistry = meterRegistry;
	}

	@Override
	public ReceitaExtraida extrair(byte[] imagem) {
		StructuredMessageCreateParams<ReceitaExtraidaIA> params = MessageCreateParams.builder()
				.model(modelo)
				.maxTokens(1024L)
				.system(PROMPT_SISTEMA)
				.outputConfig(ReceitaExtraidaIA.class)
				.addUserMessageOfBlockParams(List.of(
						ContentBlockParam.ofImage(construirBlocoImagem(imagem)),
						ContentBlockParam.ofText(TextBlockParam.builder().text(INSTRUCAO_USUARIO).build())))
				.build();

		StructuredMessage<ReceitaExtraidaIA> resposta;
		Timer.Sample amostra = Timer.start(meterRegistry);
		try {
			resposta = client.messages().create(params);
			amostra.stop(Timer.builder("ia.extracao.latencia")
					.tag("outcome", "sucesso")
					.description("Latência da chamada ao modelo de visão para extração de receita")
					.register(meterRegistry));
		} catch (AnthropicException e) {
			amostra.stop(Timer.builder("ia.extracao.latencia")
					.tag("outcome", "falha")
					.description("Latência da chamada ao modelo de visão para extração de receita")
					.register(meterRegistry));

			throw new ExtracaoReceitaFalhouException("Falha ao chamar o modelo de visão", e);
		}

		if (resposta.stopReason().filter(StopReason.REFUSAL::equals).isPresent()) {
			throw new ExtracaoReceitaFalhouException("O modelo recusou processar a imagem da receita");
		}

		ReceitaExtraidaIA extraida = resposta.content().stream()
				.flatMap(bloco -> bloco.text().stream())
				.findFirst()
				.map(StructuredTextBlock::text)
				.orElseThrow(
						() -> new ExtracaoReceitaFalhouException("Resposta da IA não trouxe um bloco de texto estruturado"));

		return new ReceitaExtraida(extraida.medicamento, extraida.dose, extraida.frequenciaHoras, extraida.duracaoDias);
	}

	private ImageBlockParam construirBlocoImagem(byte[] imagem) {
		String base64 = Base64.getEncoder().encodeToString(imagem);
		return ImageBlockParam.builder()
				.source(Base64ImageSource.builder()
						.data(base64)
						.mediaType(detectarMediaType(imagem))
						.build())
				.build();
	}

	private Base64ImageSource.MediaType detectarMediaType(byte[] imagem) {
		if (imagem.length >= 8
				&& (imagem[0] & 0xFF) == 0x89
				&& imagem[1] == 'P'
				&& imagem[2] == 'N'
				&& imagem[3] == 'G') {
			return Base64ImageSource.MediaType.IMAGE_PNG;
		}
		if (imagem.length >= 3 && (imagem[0] & 0xFF) == 0xFF && (imagem[1] & 0xFF) == 0xD8) {
			return Base64ImageSource.MediaType.IMAGE_JPEG;
		}
		if (imagem.length >= 6 && imagem[0] == 'G' && imagem[1] == 'I' && imagem[2] == 'F') {
			return Base64ImageSource.MediaType.IMAGE_GIF;
		}
		if (imagem.length >= 12 && imagem[8] == 'W' && imagem[9] == 'E' && imagem[10] == 'B' && imagem[11] == 'P') {
			return Base64ImageSource.MediaType.IMAGE_WEBP;
		}

		throw new ImagemReceitaInvalidaException(
				"Formato de imagem não suportado. Envie a foto em JPEG, PNG, GIF ou WEBP");
	}
}
