package com.dosealerta.mensageria.core.rules.respostamensagem;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static com.dosealerta.mensageria.MensageriaFixtures.VALOR_EM_BRANCO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RespostaMensagemRulesTest extends TesteUnitarioBase {

	private RespostaMensagemTelefoneDevePreenchidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new RespostaMensagemTelefoneDevePreenchidoRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new RespostaMensagemContext(TELEFONE, null, null)));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(TelefoneObrigatorioException.class, () -> telefoneRule.validar(new RespostaMensagemContext(null, null, null)));
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new RespostaMensagemContext(VALOR_EM_BRANCO, null, null)));
	}
}
