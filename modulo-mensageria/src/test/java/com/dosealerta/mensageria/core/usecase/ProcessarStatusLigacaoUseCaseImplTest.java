package com.dosealerta.mensageria.core.usecase;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.rules.statusligacao.StatusLigacaoTelefoneDevePreenchidoRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ProcessarStatusLigacaoUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private AlarmeClientGateway alarmeClientGateway;

	@Mock
	private LogGateway logGateway;

	private ProcessarStatusLigacaoUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarStatusLigacaoUseCaseImpl(
				alarmeClientGateway, logGateway, List.of(new StatusLigacaoTelefoneDevePreenchidoRule()));
	}

	@Test
	void deveRegistrarLigacaoAtendidaQuandoStatusEmAndamento() {
		useCase.executar(TELEFONE, "in-progress");

		verify(alarmeClientGateway).registrarLigacaoAtendida(TELEFONE);
	}

	@Test
	void naoDeveRegistrarNadaParaOutrosStatus() {
		useCase.executar(TELEFONE, "completed");
		useCase.executar(TELEFONE, "no-answer");
		useCase.executar(TELEFONE, "busy");

		verify(alarmeClientGateway, never()).registrarLigacaoAtendida(anyString());
	}

	@Test
	void naoDeveRegistrarNadaQuandoTelefoneAusente() {
		useCase.executar(null, "in-progress");
		useCase.executar("", "in-progress");
		useCase.executar("  ", "in-progress");

		verify(alarmeClientGateway, never()).registrarLigacaoAtendida(anyString());
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarLigacaoAtendida(anyString());

		useCase.executar(TELEFONE, "in-progress");
	}
}
