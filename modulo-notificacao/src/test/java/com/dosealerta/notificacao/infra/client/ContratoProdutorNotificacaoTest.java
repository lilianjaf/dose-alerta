package com.dosealerta.notificacao.infra.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoProdutorNotificacaoTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void enviar_mensagem_payloadEmitidoRespeitaOContrato() {
		Contrato contrato = Contrato.carregar("enviar-mensagem");
		var exemplo = contrato.exemploComoArvore();
		Object request = new EnviarMensagemRequest(exemplo.get("telefone").asString(), exemplo.get("texto").asString(), exemplo.get("textoBotao").asString());
		String json = MAPPER.writeValueAsString(request);
		assertTrue(contrato.violacoes(json).isEmpty(), () -> "payload fora do contrato: " + contrato.violacoes(json) + " -> " + json);
	}

	@Test
	void realizar_ligacao_payloadEmitidoRespeitaOContrato() {
		Contrato contrato = Contrato.carregar("realizar-ligacao");
		var exemplo = contrato.exemploComoArvore();
		Object request = new RealizarLigacaoRequest(exemplo.get("telefone").asString(), exemplo.get("textoFalado").asString());
		String json = MAPPER.writeValueAsString(request);
		assertTrue(contrato.violacoes(json).isEmpty(), () -> "payload fora do contrato: " + contrato.violacoes(json) + " -> " + json);
	}

}
