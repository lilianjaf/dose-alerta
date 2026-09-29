package com.dosealerta.mensageria.core.rules.mensagemrecebida;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static com.dosealerta.mensageria.MensageriaFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.mensageria.MensageriaFixtures.umaMensagemDoTelefone;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MensagemRecebidaRulesTest extends TesteUnitarioBase {

	private MensagemRecebidaTelefoneDevePreenchidoRule telefoneRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new MensagemRecebidaTelefoneDevePreenchidoRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchido() {
		assertDoesNotThrow(() -> telefoneRule.validar(new MensagemRecebidaContext(umaMensagemDoTelefone(TELEFONE))));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(TelefoneObrigatorioException.class, () -> telefoneRule.validar(new MensagemRecebidaContext(umaMensagemDoTelefone(null))));
		assertThrows(
				TelefoneObrigatorioException.class, () -> telefoneRule.validar(new MensagemRecebidaContext(umaMensagemDoTelefone(VALOR_EM_BRANCO))));
	}
}
