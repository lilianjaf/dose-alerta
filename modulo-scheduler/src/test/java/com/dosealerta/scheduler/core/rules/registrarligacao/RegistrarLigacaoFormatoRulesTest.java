package com.dosealerta.scheduler.core.rules.registrarligacao;

import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.exception.TelefoneFormatoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrarLigacaoFormatoRulesTest extends TesteUnitarioBase {

	private static final String TELEFONE_INVALIDO = "numero-invalido";

	private RegistrarLigacaoTelefoneDeveTerFormatoValidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new RegistrarLigacaoTelefoneDeveTerFormatoValidoRule();
	}

	@Test
	void devePassarQuandoTelefoneEstaEmFormatoValido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new RegistroLigacaoContext(TELEFONE, null)));
	}

	@Test
	void deveRejeitarTelefoneForaDoFormato() {
		assertThrows(TelefoneFormatoInvalidoException.class, () -> telefoneRule.validar(new RegistroLigacaoContext(TELEFONE_INVALIDO, null)));
	}

	@Test
	void deveIgnorarTelefoneNuloPoisOutraRegraCuidaDisso() {
		assertDoesNotThrow(() -> telefoneRule.validar(new RegistroLigacaoContext(null, null)));
	}
}
