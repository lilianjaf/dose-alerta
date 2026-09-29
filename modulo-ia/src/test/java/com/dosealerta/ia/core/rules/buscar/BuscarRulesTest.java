package com.dosealerta.ia.core.rules.buscar;

import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.exception.ReceitaIdObrigatorioException;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuscarRulesTest extends TesteUnitarioBase {

	private BuscarReceitaIdDeveSerInformadoRule idRule;
	private BuscarReceitaDeveExistirRule existeRule;

	@BeforeEach
	void setUp() {
		idRule = new BuscarReceitaIdDeveSerInformadoRule();
		existeRule = new BuscarReceitaDeveExistirRule();
	}

	@Test
	void devePassarQuandoIdInformadoEReceitaExiste() {
		BuscaReceitaContext contexto = new BuscaReceitaContext(RECEITA_ID, umaReceita());

		assertDoesNotThrow(() -> idRule.validar(contexto));
		assertDoesNotThrow(() -> existeRule.validar(contexto));
	}

	@Test
	void deveRejeitarIdNulo() {
		assertThrows(
				ReceitaIdObrigatorioException.class, () -> idRule.validar(new BuscaReceitaContext(null, null)));
	}

	@Test
	void deveRejeitarReceitaInexistente() {
		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> existeRule.validar(new BuscaReceitaContext(RECEITA_ID, null)));
	}
}
