package com.dosealerta.scheduler.core.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Teste de contrato (Etapa 11.2) — consumidor: modulo-scheduler aceita o que ia e mensageria enviam.
 * Fonte da verdade: {@code contratos/src/main/resources/contratos/*.schema.json}.
 */
class ContratoConsumidorSchedulerTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void criar_alarme_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("criar-alarme");
		CriarAlarmeInput input = MAPPER.readValue(contrato.exemplo(), CriarAlarmeInput.class);
		assertTrue(VALIDATOR.validate(input).isEmpty());
	}

	@Test
	void criar_alarme_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("criar-alarme");
		for (String campo : contrato.camposObrigatorios()) {
			CriarAlarmeInput input = MAPPER.readValue(contrato.semCampo(campo), CriarAlarmeInput.class);
			assertFalse(VALIDATOR.validate(input).isEmpty(), "consumidor aceitou payload sem " + campo);
		}
	}

	@Test
	void resposta_paciente_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("resposta-paciente");
		RegistrarInteracaoPorTelefoneInput input = MAPPER.readValue(contrato.exemplo(), RegistrarInteracaoPorTelefoneInput.class);
		assertTrue(VALIDATOR.validate(input).isEmpty());
	}

	@Test
	void resposta_paciente_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("resposta-paciente");
		for (String campo : contrato.camposObrigatorios()) {
			RegistrarInteracaoPorTelefoneInput input = MAPPER.readValue(contrato.semCampo(campo), RegistrarInteracaoPorTelefoneInput.class);
			assertFalse(VALIDATOR.validate(input).isEmpty(), "consumidor aceitou payload sem " + campo);
		}
	}

}
