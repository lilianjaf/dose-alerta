package com.dosealerta.notificacao.core.dto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.contratos.Contrato;
import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitacaoEnvioContext;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioAlarmeIdDeveSerInformadoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioDoseDevePreenchidaRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioEtapaDeveSerInformadaRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioMedicamentoDevePreenchidoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioPacienteIdDeveSerInformadoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioTelefoneDevePreenchidoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.ValidadorSolicitacaoEnvioRule;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContratoConsumidorNotificacaoTest extends TesteUnitarioBase {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	void solicitar_envio_exemploCanonicoEAceitoPeloConsumidor() throws Exception {
		Contrato contrato = Contrato.carregar("solicitar-envio");
		SolicitarEnvioInput input = MAPPER.readValue(contrato.exemplo(), SolicitarEnvioInput.class);
		assertDoesNotThrow(() -> validar(input));
	}

	@Test
	void solicitar_envio_consumidorRejeitaPayloadSemQualquerCampoObrigatorio() throws Exception {
		Contrato contrato = Contrato.carregar("solicitar-envio");
		for (String campo : contrato.camposObrigatorios()) {
			SolicitarEnvioInput input = MAPPER.readValue(contrato.semCampo(campo), SolicitarEnvioInput.class);
			assertThrows(RuntimeException.class, () -> validar(input), "consumidor aceitou payload sem " + campo);
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

	private void validar(SolicitarEnvioInput input) {
		List<ValidadorSolicitacaoEnvioRule> regras = List.of(
				new SolicitarEnvioAlarmeIdDeveSerInformadoRule(),
				new SolicitarEnvioPacienteIdDeveSerInformadoRule(),
				new SolicitarEnvioTelefoneDevePreenchidoRule(),
				new SolicitarEnvioTelefoneDeveTerFormatoValidoRule(),
				new SolicitarEnvioMedicamentoDevePreenchidoRule(),
				new SolicitarEnvioDoseDevePreenchidaRule(),
				new SolicitarEnvioEtapaDeveSerInformadaRule());
		SolicitacaoEnvioContext context = new SolicitacaoEnvioContext(input);
		regras.forEach(regra -> regra.validar(context));
	}
}
