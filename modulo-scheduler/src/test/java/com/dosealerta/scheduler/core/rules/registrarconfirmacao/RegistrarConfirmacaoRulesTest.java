package com.dosealerta.scheduler.core.rules.registrarconfirmacao;

import static com.dosealerta.scheduler.SchedulerFixtures.CORRELATION_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static com.dosealerta.scheduler.SchedulerFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarmeEnviado;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.exception.AlarmeJaConfirmadoException;
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrarConfirmacaoRulesTest extends TesteUnitarioBase {

	private RegistrarConfirmacaoTelefoneDevePreenchidoRule telefoneRule;
	private RegistrarConfirmacaoAlarmePendenteDeveExistirRule pendenteRule;
	private RegistrarConfirmacaoAlarmeNaoDeveEstarConfirmadoRule confirmadoRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new RegistrarConfirmacaoTelefoneDevePreenchidoRule();
		pendenteRule = new RegistrarConfirmacaoAlarmePendenteDeveExistirRule();
		confirmadoRule = new RegistrarConfirmacaoAlarmeNaoDeveEstarConfirmadoRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchidoEAlarmePendenteExiste() {
		Alarme alarme = umAlarme();
		RegistroConfirmacaoContext contexto = new RegistroConfirmacaoContext(TELEFONE, alarme);

		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> pendenteRule.validar(contexto));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new RegistroConfirmacaoContext(null, null)));
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new RegistroConfirmacaoContext(VALOR_EM_BRANCO, null)));
	}

	@Test
	void deveRejeitarQuandoNaoHaAlarmePendente() {
		assertThrows(
				AlarmePendenteNaoEncontradoException.class,
				() -> pendenteRule.validar(new RegistroConfirmacaoContext(TELEFONE, null)));
	}

	@Test
	void deveRejeitarAlarmeJaConfirmado() {
		Alarme confirmado = umAlarmeEnviado(EtapaEscalonamento.LEMBRETE_INICIAL);
		confirmado.confirmar(INSTANTE_FIXO, CORRELATION_ID);

		assertThrows(
				AlarmeJaConfirmadoException.class,
				() -> confirmadoRule.validar(new RegistroConfirmacaoContext(TELEFONE, confirmado)));
	}
}
