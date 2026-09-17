package com.dosealerta.ia.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.core.http.Headers;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredContentBlock;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import com.anthropic.services.blocking.MessageService;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClaudeExtratorReceitaGatewayTest {

	@Test
	void deveMapearARespostaEstruturadaParaOTipoDeDominio() {
		AnthropicClient client = mock(AnthropicClient.class);
		MessageService messageService = mock(MessageService.class);
		when(client.messages()).thenReturn(messageService);

		ReceitaExtraidaIA extraidaIA = new ReceitaExtraidaIA();
		extraidaIA.medicamento = "Losartana";
		extraidaIA.dose = "50mg";
		extraidaIA.frequenciaHoras = 24;
		extraidaIA.duracaoDias = 30;

		StructuredTextBlock<ReceitaExtraidaIA> textBlock = mock(StructuredTextBlock.class);
		when(textBlock.text()).thenReturn(extraidaIA);

		StructuredContentBlock<ReceitaExtraidaIA> contentBlock = mock(StructuredContentBlock.class);
		when(contentBlock.text()).thenReturn(Optional.of(textBlock));

		StructuredMessage<ReceitaExtraidaIA> resposta = mock(StructuredMessage.class);
		when(resposta.stopReason()).thenReturn(Optional.of(StopReason.END_TURN));
		when(resposta.content()).thenReturn(List.of(contentBlock));

		when(messageService.create(any(StructuredMessageCreateParams.class))).thenReturn(resposta);

		ClaudeExtratorReceitaGateway gateway = new ClaudeExtratorReceitaGateway(client, "claude-opus-5");
		ReceitaExtraida resultado = gateway.extrair(imagemJpegMinima());

		assertEquals(new ReceitaExtraida("Losartana", "50mg", 24, 30), resultado);
	}

	@Test
	void deveLancarExcecaoQuandoOModeloRecusaProcessarAImagem() {
		AnthropicClient client = mock(AnthropicClient.class);
		MessageService messageService = mock(MessageService.class);
		when(client.messages()).thenReturn(messageService);

		StructuredMessage<ReceitaExtraidaIA> resposta = mock(StructuredMessage.class);
		when(resposta.stopReason()).thenReturn(Optional.of(StopReason.REFUSAL));

		when(messageService.create(any(StructuredMessageCreateParams.class))).thenReturn(resposta);

		ClaudeExtratorReceitaGateway gateway = new ClaudeExtratorReceitaGateway(client, "claude-opus-5");

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoAChamadaFalha() {
		AnthropicClient client = mock(AnthropicClient.class);
		MessageService messageService = mock(MessageService.class);
		when(client.messages()).thenReturn(messageService);
		when(messageService.create(any(StructuredMessageCreateParams.class)))
				.thenThrow(RateLimitException.builder()
						.headers(Headers.builder().build())
						.body(JsonValue.from(Map.of()))
						.build());

		ClaudeExtratorReceitaGateway gateway = new ClaudeExtratorReceitaGateway(client, "claude-opus-5");

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoFalhaDeRedeOcorre() {
		AnthropicClient client = mock(AnthropicClient.class);
		MessageService messageService = mock(MessageService.class);
		when(client.messages()).thenReturn(messageService);
		when(messageService.create(any(StructuredMessageCreateParams.class)))
				.thenThrow(new AnthropicIoException("timeout", new IOException("timeout")));

		ClaudeExtratorReceitaGateway gateway = new ClaudeExtratorReceitaGateway(client, "claude-opus-5");

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveRejeitarFormatoDeImagemNaoReconhecido() {
		AnthropicClient client = mock(AnthropicClient.class);

		ClaudeExtratorReceitaGateway gateway = new ClaudeExtratorReceitaGateway(client, "claude-opus-5");

		assertThrows(ImagemReceitaInvalidaException.class, () -> gateway.extrair(imagemHeicMinima()));
	}

	private byte[] imagemJpegMinima() {
		return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0};
	}

	private byte[] imagemHeicMinima() {
		return new byte[] {0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c', 0, 0, 0, 0};
	}
}
