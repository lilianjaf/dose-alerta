package com.dosealerta.ia.core.rules.confirmarportelefone;

import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static com.dosealerta.ia.IaFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfirmarPorTelefoneRulesTest extends TesteUnitarioBase {

	private ConfirmarPorTelefoneTelefoneDevePreenchidoRule telefoneRule;
	private ConfirmarPorTelefoneReceitaPendenteDeveExistirRule pendenteRule;

	@BeforeEach
	void setUp() {
		telefoneRule = new ConfirmarPorTelefoneTelefoneDevePreenchidoRule();
		pendenteRule = new ConfirmarPorTelefoneReceitaPendenteDeveExistirRule();
	}

	@Test
	void devePassarQuandoTelefonePreenchidoEReceitaPendenteExiste() {
		ConfirmacaoPorTelefoneContext contexto = new ConfirmacaoPorTelefoneContext(TELEFONE, umaReceita());

		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> pendenteRule.validar(contexto));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(new ConfirmacaoPorTelefoneContext(null, null)));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(new ConfirmacaoPorTelefoneContext(VALOR_EM_BRANCO, null)));
	}

	@Test
	void deveRejeitarQuandoNaoHaReceitaPendente() {
		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> pendenteRule.validar(new ConfirmacaoPorTelefoneContext(TELEFONE, null)));
	}
}
