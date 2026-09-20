package com.dosealerta.mensageria.infra.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Teste de contrato (Etapa 11.2) — produtor: modulo-mensageria avisa o modulo-scheduler da resposta do paciente.
 * Fonte da verdade: {@code contratos/src/main/resources/contratos/*.schema.json}.
 */
class ContratoProdutorMensageriaTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void resposta_paciente_payloadEmitidoRespeitaOContrato() {
		Contrato contrato = Contrato.carregar("resposta-paciente");
		var exemplo = contrato.exemploComoArvore();
		Object request = new TelefoneRequest(exemplo.get("telefone").asString());
		String json = MAPPER.writeValueAsString(request);
		assertTrue(contrato.violacoes(json).isEmpty(), () -> "payload fora do contrato: " + contrato.violacoes(json) + " -> " + json);
	}

}
