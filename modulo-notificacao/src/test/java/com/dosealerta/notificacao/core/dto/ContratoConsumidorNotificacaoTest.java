package com.dosealerta.notificacao.core.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

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
		Set<String> doCodigo = new TreeSet<>();
		for (EtapaEscalonamento valor : EtapaEscalonamento.values()) {
			doCodigo.add(valor.name());
		}
		assertEquals(Contrato.carregar("solicitar-envio").valoresPermitidos("etapa"), doCodigo);
	}
}
