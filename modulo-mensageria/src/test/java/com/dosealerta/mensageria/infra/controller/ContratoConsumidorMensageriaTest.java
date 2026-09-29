package com.dosealerta.mensageria.infra.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import com.dosealerta.mensageria.TesteUnitarioBase;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoConsumidorMensageriaTest extends TesteUnitarioBase {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void enviar_mensagem_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("enviar-mensagem");
		EnviarMensagemRequest input = MAPPER.readValue(contrato.exemplo(), EnviarMensagemRequest.class);
		assertTrue(VALIDATOR.validate(input).isEmpty());
	}

	@Test
	void enviar_mensagem_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("enviar-mensagem");
		for (String campo : contrato.camposObrigatorios()) {
			EnviarMensagemRequest input = MAPPER.readValue(contrato.semCampo(campo), EnviarMensagemRequest.class);
			assertFalse(VALIDATOR.validate(input).isEmpty(), "consumidor aceitou payload sem " + campo);
		}
	}

	@Test
	void realizar_ligacao_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("realizar-ligacao");
		RealizarLigacaoRequest input = MAPPER.readValue(contrato.exemplo(), RealizarLigacaoRequest.class);
		assertTrue(VALIDATOR.validate(input).isEmpty());
	}

	@Test
	void realizar_ligacao_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("realizar-ligacao");
		for (String campo : contrato.camposObrigatorios()) {
			RealizarLigacaoRequest input = MAPPER.readValue(contrato.semCampo(campo), RealizarLigacaoRequest.class);
			assertFalse(VALIDATOR.validate(input).isEmpty(), "consumidor aceitou payload sem " + campo);
		}
	}

}
