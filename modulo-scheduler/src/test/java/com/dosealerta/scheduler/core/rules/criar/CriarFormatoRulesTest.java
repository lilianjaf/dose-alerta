package com.dosealerta.scheduler.core.rules.criar;

import static com.dosealerta.scheduler.SchedulerFixtures.DOSE;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.MEDICAMENTO;
import static com.dosealerta.scheduler.SchedulerFixtures.PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoCom;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CriarFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";

	private CriarTelefoneDeveTerFormatoValidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new CriarTelefoneDeveTerFormatoValidoRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new CriacaoAlarmeContext(umaCriacaoCom(PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new CriacaoAlarmeContext(umaCriacaoCom(PACIENTE_ID, TELEFONE_INVALIDO, MEDICAMENTO, DOSE, INSTANTE_FIXO))));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new CriacaoAlarmeContext(umaCriacaoCom(PACIENTE_ID, null, MEDICAMENTO, DOSE, INSTANTE_FIXO))));
	}
}
