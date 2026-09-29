package com.dosealerta.mensageria.core.rules.statusligacao;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static com.dosealerta.mensageria.MensageriaFixtures.VALOR_EM_BRANCO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusLigacaoRulesTest extends TesteUnitarioBase {

	private StatusLigacaoTelefoneDevePreenchidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new StatusLigacaoTelefoneDevePreenchidoRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new StatusLigacaoContext(TELEFONE, null)));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(TelefoneObrigatorioException.class, () -> telefoneRule.validar(new StatusLigacaoContext(null, null)));
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new StatusLigacaoContext(VALOR_EM_BRANCO, null)));
	}
}
