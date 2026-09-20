package com.dosealerta.relatorioadesao.core.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Teste de contrato (Etapa 11.2) — consumidor: modulo-relatorio-adesao aceita o que o modulo-scheduler envia.
 * Fonte da verdade: {@code contratos/src/main/resources/contratos/*.schema.json}.
 */
class ContratoConsumidorRelatorioTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void registrar_interacao_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("registrar-interacao");
		RegistrarInteracaoInput input = MAPPER.readValue(contrato.exemplo(), RegistrarInteracaoInput.class);
		assertTrue(VALIDATOR.validate(input).isEmpty());
	}

	@Test
	void registrar_interacao_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("registrar-interacao");
		for (String campo : contrato.camposObrigatorios()) {
			RegistrarInteracaoInput input = MAPPER.readValue(contrato.semCampo(campo), RegistrarInteracaoInput.class);
			assertFalse(VALIDATOR.validate(input).isEmpty(), "consumidor aceitou payload sem " + campo);
		}
	}

	@Test
	void registrar_interacao_tipo_valoresDoEnumSaoOsMesmosDoContrato() {
		java.util.Set<String> doCodigo = new java.util.TreeSet<>();
		for (com.dosealerta.relatorioadesao.core.domain.TipoInteracao valor : com.dosealerta.relatorioadesao.core.domain.TipoInteracao.values()) {
			doCodigo.add(valor.name());
		}
		assertEquals(Contrato.carregar("registrar-interacao").valoresPermitidos("tipo"), doCodigo);
	}
}
