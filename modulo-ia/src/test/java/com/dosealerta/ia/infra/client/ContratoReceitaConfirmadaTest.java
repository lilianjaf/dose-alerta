package com.dosealerta.ia.infra.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import com.dosealerta.ia.TesteUnitarioBase;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoReceitaConfirmadaTest extends TesteUnitarioBase {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void criar_alarme_payloadEmitidoRespeitaOContrato() {
		Contrato contrato = Contrato.carregar("criar-alarme");
		var exemplo = contrato.exemploComoArvore();
		Object request = new CriarAlarmeRequest(UUID.fromString(exemplo.get("pacienteId").asString()), exemplo.get("telefone").asString(), exemplo.get("medicamento").asString(), exemplo.get("dose").asString(), Instant.parse(exemplo.get("horarioAlvo").asString()));
		String json = MAPPER.writeValueAsString(request);
		assertTrue(contrato.violacoes(json).isEmpty(), () -> "payload fora do contrato: " + contrato.violacoes(json) + " -> " + json);
	}

}
