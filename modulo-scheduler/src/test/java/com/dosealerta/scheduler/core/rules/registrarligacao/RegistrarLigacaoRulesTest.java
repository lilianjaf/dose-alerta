package com.dosealerta.scheduler.core.rules.registrarligacao;

import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static com.dosealerta.scheduler.SchedulerFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrarLigacaoRulesTest extends TesteUnitarioBase {

	private RegistrarLigacaoTelefoneDevePreenchidoRule telefoneRule;
	private RegistrarLigacaoAlarmePendenteDeveExistirRule pendenteRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new RegistrarLigacaoTelefoneDevePreenchidoRule();
		pendenteRule = new RegistrarLigacaoAlarmePendenteDeveExistirRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchidoEAlarmePendenteExiste() {
		Alarme alarme = umAlarme();
		RegistroLigacaoContext contexto = new RegistroLigacaoContext(TELEFONE, alarme);

		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> pendenteRule.validar(contexto));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new RegistroLigacaoContext(null, null)));
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new RegistroLigacaoContext(VALOR_EM_BRANCO, null)));
	}

	@Test
	void deveRejeitarQuandoNaoHaAlarmePendente() {
		assertThrows(
				AlarmePendenteNaoEncontradoException.class,
				() -> pendenteRule.validar(new RegistroLigacaoContext(TELEFONE, null)));
	}
}
