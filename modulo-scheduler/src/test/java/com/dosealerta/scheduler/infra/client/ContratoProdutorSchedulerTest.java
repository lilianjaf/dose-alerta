package com.dosealerta.scheduler.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.contratos.Contrato;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoProdutorSchedulerTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void solicitar_envio_payloadEmitidoRespeitaOContrato() {
		Contrato contrato = Contrato.carregar("solicitar-envio");
		var exemplo = contrato.exemploComoArvore();
		Object request = new SolicitarEnvioRequest(java.util.UUID.fromString(exemplo.get("alarmeId").asString()), java.util.UUID.fromString(exemplo.get("pacienteId").asString()), exemplo.get("telefone").asString(), exemplo.get("medicamento").asString(), exemplo.get("dose").asString(), EtapaEscalonamento.valueOf(exemplo.get("etapa").asString()));
		String json = MAPPER.writeValueAsString(request);
		assertTrue(contrato.violacoes(json).isEmpty(), () -> "payload fora do contrato: " + contrato.violacoes(json) + " -> " + json);
	}

	@Test
	void registrar_interacao_payloadEmitidoRespeitaOContrato() {
		Contrato contrato = Contrato.carregar("registrar-interacao");
		var exemplo = contrato.exemploComoArvore();
		Object request = new RegistrarInteracaoRequest(java.util.UUID.fromString(exemplo.get("id").asString()), java.util.UUID.fromString(exemplo.get("alarmeId").asString()), java.util.UUID.fromString(exemplo.get("pacienteId").asString()), exemplo.get("medicamento").asString(), TipoInteracao.valueOf(exemplo.get("tipo").asString()), java.time.Instant.parse(exemplo.get("registradaEm").asString()));
		String json = MAPPER.writeValueAsString(request);
		assertTrue(contrato.violacoes(json).isEmpty(), () -> "payload fora do contrato: " + contrato.violacoes(json) + " -> " + json);
	}

	@Test
	void solicitar_envio_etapa_valoresDoEnumSaoOsMesmosDoContrato() {
		java.util.Set<String> doCodigo = new java.util.TreeSet<>();
		for (EtapaEscalonamento valor : EtapaEscalonamento.values()) {
			doCodigo.add(valor.name());
		}
		assertEquals(Contrato.carregar("solicitar-envio").valoresPermitidos("etapa"), doCodigo);
	}

	@Test
	void registrar_interacao_tipo_valoresDoEnumSaoOsMesmosDoContrato() {
		java.util.Set<String> doCodigo = new java.util.TreeSet<>();
		for (TipoInteracao valor : TipoInteracao.values()) {
			doCodigo.add(valor.name());
		}
		assertEquals(Contrato.carregar("registrar-interacao").valoresPermitidos("tipo"), doCodigo);
	}
}
