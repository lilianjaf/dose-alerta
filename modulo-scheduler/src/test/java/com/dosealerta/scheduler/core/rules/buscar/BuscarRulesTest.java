package com.dosealerta.scheduler.core.rules.buscar;

import static com.dosealerta.scheduler.SchedulerFixtures.ALARME_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.exception.AlarmeIdObrigatorioException;
import com.dosealerta.scheduler.core.exception.AlarmeNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuscarRulesTest extends TesteUnitarioBase {

	private BuscarAlarmeIdDeveSerInformadoRule idRule;
	private BuscarAlarmeDeveExistirRule existeRule;

	@BeforeEach
	void setUp() {
		idRule = new BuscarAlarmeIdDeveSerInformadoRule();
		existeRule = new BuscarAlarmeDeveExistirRule();
	}

	@Test
	void devePassarQuandoIdInformadoEAlarmeExiste() {
		BuscaAlarmeContext contexto = new BuscaAlarmeContext(ALARME_ID, umAlarme());

		assertDoesNotThrow(() -> idRule.validar(contexto));
		assertDoesNotThrow(() -> existeRule.validar(contexto));
	}

	@Test
	void deveRejeitarIdNulo() {
		assertThrows(AlarmeIdObrigatorioException.class, () -> idRule.validar(new BuscaAlarmeContext(null, null)));
	}

	@Test
	void deveRejeitarAlarmeInexistente() {
		assertThrows(
				AlarmeNaoEncontradoException.class, () -> existeRule.validar(new BuscaAlarmeContext(ALARME_ID, null)));
	}
}
