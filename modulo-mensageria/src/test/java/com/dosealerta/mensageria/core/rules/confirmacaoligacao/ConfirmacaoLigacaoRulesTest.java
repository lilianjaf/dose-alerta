package com.dosealerta.mensageria.core.rules.confirmacaoligacao;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static com.dosealerta.mensageria.MensageriaFixtures.VALOR_EM_BRANCO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfirmacaoLigacaoRulesTest extends TesteUnitarioBase {

	private ConfirmacaoLigacaoTelefoneDevePreenchidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new ConfirmacaoLigacaoTelefoneDevePreenchidoRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new ConfirmacaoLigacaoContext(TELEFONE, null)));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(TelefoneObrigatorioException.class, () -> telefoneRule.validar(new ConfirmacaoLigacaoContext(null, null)));
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new ConfirmacaoLigacaoContext(VALOR_EM_BRANCO, null)));
	}
}
