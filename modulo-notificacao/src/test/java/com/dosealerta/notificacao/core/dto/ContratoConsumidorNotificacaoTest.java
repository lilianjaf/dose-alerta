package com.dosealerta.notificacao.core.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Teste de contrato (Etapa 11.2) — consumidor: modulo-notificacao aceita o que o modulo-scheduler envia.
 * Fonte da verdade: {@code contratos/src/main/resources/contratos/*.schema.json}.
 */
class ContratoConsumidorNotificacaoTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void solicitar_envio_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("solicitar-envio");
		SolicitarEnvioInput input = MAPPER.readValue(contrato.exemplo(), SolicitarEnvioInput.class);
		assertTrue(VALIDATOR.validate(input).isEmpty());
	}

	@Test
	void solicitar_envio_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("solicitar-envio");
		for (String campo : contrato.camposObrigatorios()) {
			SolicitarEnvioInput input = MAPPER.readValue(contrato.semCampo(campo), SolicitarEnvioInput.class);
			assertFalse(VALIDATOR.validate(input).isEmpty(), "consumidor aceitou payload sem " + campo);
		}
	}

	@Test
	void solicitar_envio_etapa_valoresDoEnumSaoOsMesmosDoContrato() {
		java.util.Set<String> doCodigo = new java.util.TreeSet<>();
		for (com.dosealerta.notificacao.core.domain.EtapaEscalonamento valor : com.dosealerta.notificacao.core.domain.EtapaEscalonamento.values()) {
			doCodigo.add(valor.name());
		}
		assertEquals(Contrato.carregar("solicitar-envio").valoresPermitidos("etapa"), doCodigo);
	}
}
