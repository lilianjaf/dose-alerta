package com.dosealerta.notificacao.core.rules.solicitarenvio;

import static com.dosealerta.notificacao.NotificacaoFixtures.ALARME_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.DOSE;
import static com.dosealerta.notificacao.NotificacaoFixtures.MEDICAMENTO;
import static com.dosealerta.notificacao.NotificacaoFixtures.PACIENTE_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.TELEFONE;
import static com.dosealerta.notificacao.NotificacaoFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoCom;
import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoValida;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.exception.AlarmeIdObrigatorioException;
import com.dosealerta.notificacao.core.exception.DoseObrigatoriaException;
import com.dosealerta.notificacao.core.exception.EtapaObrigatoriaException;
import com.dosealerta.notificacao.core.exception.MedicamentoObrigatorioException;
import com.dosealerta.notificacao.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.notificacao.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolicitarEnvioRulesTest extends TesteUnitarioBase {

	private static final EtapaEscalonamento ETAPA = EtapaEscalonamento.LEMBRETE_INICIAL;

	private SolicitarEnvioAlarmeIdDeveSerInformadoRule alarmeRule;
	private SolicitarEnvioPacienteIdDeveSerInformadoRule pacienteRule;
	private SolicitarEnvioTelefoneDevePreenchidoRule telefoneRule;
	private SolicitarEnvioMedicamentoDevePreenchidoRule medicamentoRule;
	private SolicitarEnvioDoseDevePreenchidaRule doseRule;
	private SolicitarEnvioEtapaDeveSerInformadaRule etapaRule;

	@BeforeEach
	void setUp() {
		alarmeRule = new SolicitarEnvioAlarmeIdDeveSerInformadoRule();
		pacienteRule = new SolicitarEnvioPacienteIdDeveSerInformadoRule();
		telefoneRule = new SolicitarEnvioTelefoneDevePreenchidoRule();
		medicamentoRule = new SolicitarEnvioMedicamentoDevePreenchidoRule();
		doseRule = new SolicitarEnvioDoseDevePreenchidaRule();
		etapaRule = new SolicitarEnvioEtapaDeveSerInformadaRule();
	}

	private SolicitacaoEnvioContext contexto(SolicitarEnvioInput input) {
		return new SolicitacaoEnvioContext(input);
	}

	@Test
	void devePassarQuandoTudoValido() {
		SolicitacaoEnvioContext contexto = contexto(umaSolicitacaoValida());

		assertDoesNotThrow(() -> alarmeRule.validar(contexto));
		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> medicamentoRule.validar(contexto));
		assertDoesNotThrow(() -> doseRule.validar(contexto));
		assertDoesNotThrow(() -> etapaRule.validar(contexto));
	}

	@Test
	void deveRejeitarAlarmeIdNulo() {
		assertThrows(
				AlarmeIdObrigatorioException.class,
				() -> alarmeRule.validar(contexto(umaSolicitacaoCom(null, PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, ETAPA))));
	}

	@Test
	void deveRejeitarPacienteIdNulo() {
		assertThrows(
				PacienteIdObrigatorioException.class,
				() -> pacienteRule.validar(contexto(umaSolicitacaoCom(ALARME_ID, null, TELEFONE, MEDICAMENTO, DOSE, ETAPA))));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, null, MEDICAMENTO, DOSE, ETAPA))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(
						contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, VALOR_EM_BRANCO, MEDICAMENTO, DOSE, ETAPA))));
	}

	@Test
	void deveRejeitarMedicamentoNuloOuEmBranco() {
		assertThrows(
				MedicamentoObrigatorioException.class,
				() -> medicamentoRule.validar(contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE, null, DOSE, ETAPA))));
		assertThrows(
				MedicamentoObrigatorioException.class,
				() -> medicamentoRule.validar(
						contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE, VALOR_EM_BRANCO, DOSE, ETAPA))));
	}

	@Test
	void deveRejeitarDoseNulaOuEmBranco() {
		assertThrows(
				DoseObrigatoriaException.class,
				() -> doseRule.validar(contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE, MEDICAMENTO, null, ETAPA))));
		assertThrows(
				DoseObrigatoriaException.class,
				() -> doseRule.validar(
						contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE, MEDICAMENTO, VALOR_EM_BRANCO, ETAPA))));
	}

	@Test
	void deveRejeitarEtapaNula() {
		assertThrows(
				EtapaObrigatoriaException.class,
				() -> etapaRule.validar(
						contexto(umaSolicitacaoCom(ALARME_ID, PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, null))));
	}
}
