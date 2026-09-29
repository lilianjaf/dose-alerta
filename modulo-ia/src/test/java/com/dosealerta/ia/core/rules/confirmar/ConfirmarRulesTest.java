package com.dosealerta.ia.core.rules.confirmar;

import static com.dosealerta.ia.IaFixtures.DOSE;
import static com.dosealerta.ia.IaFixtures.MEDICAMENTO;
import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static com.dosealerta.ia.IaFixtures.umaReceitaConfirmada;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.ia.core.exception.ReceitaJaConfirmadaException;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfirmarRulesTest extends TesteUnitarioBase {

	private ConfirmarReceitaDeveExistirRule existeRule;
	private ConfirmarReceitaDeveEstarAguardandoConfirmacaoRule aguardandoRule;
	private ConfirmarDadosDaReceitaDevemEstarCompletosRule completosRule;

	@BeforeEach
	void setUp() {
		existeRule = new ConfirmarReceitaDeveExistirRule();
		aguardandoRule = new ConfirmarReceitaDeveEstarAguardandoConfirmacaoRule();
		completosRule = new ConfirmarDadosDaReceitaDevemEstarCompletosRule();
	}

	private ConfirmacaoReceitaContext contexto(Receita receita, String dose, Integer frequencia, Integer duracao) {
		return new ConfirmacaoReceitaContext(RECEITA_ID, receita, MEDICAMENTO, dose, frequencia, duracao);
	}

	@Test
	void devePassarQuandoReceitaExisteAguardandoEComDadosCompletos() {
		ConfirmacaoReceitaContext contexto = contexto(umaReceita(), DOSE, 24, 30);

		assertDoesNotThrow(() -> existeRule.validar(contexto));
		assertDoesNotThrow(() -> aguardandoRule.validar(contexto));
		assertDoesNotThrow(() -> completosRule.validar(contexto));
	}

	@Test
	void deveRejeitarReceitaInexistente() {
		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> existeRule.validar(contexto(null, DOSE, 24, 30)));
	}

	@Test
	void deveRejeitarReceitaJaConfirmada() {
		assertThrows(
				ReceitaJaConfirmadaException.class,
				() -> aguardandoRule.validar(contexto(umaReceitaConfirmada(), DOSE, 24, 30)));
	}

	@Test
	void deveListarTodosOsCamposPendentes() {
		DadosReceitaIncompletosException e = assertThrows(
				DadosReceitaIncompletosException.class,
				() -> completosRule.validar(contexto(umaReceita(), VALOR_EM_BRANCO, null, null)));

		assertEquals(List.of("dose", "frequenciaHoras", "duracaoDias"), e.getCamposPendentes());
	}

	@Test
	void deveListarSomenteOsCamposQueFaltam() {
		DadosReceitaIncompletosException e = assertThrows(
				DadosReceitaIncompletosException.class,
				() -> completosRule.validar(contexto(umaReceita(), DOSE, 8, null)));

		assertEquals(List.of("duracaoDias"), e.getCamposPendentes());
	}
}
