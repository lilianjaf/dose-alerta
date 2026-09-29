package com.dosealerta.scheduler.core.usecase;

import static com.dosealerta.scheduler.SchedulerFixtures.ALARME_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.AlarmeNaoEncontradoException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.rules.buscar.BuscaAlarmeContext;
import com.dosealerta.scheduler.core.rules.buscar.ValidadorBuscaAlarmeRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class BuscarAlarmeUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Mock
	private ValidadorBuscaAlarmeRule regra;

	@Captor
	private ArgumentCaptor<BuscaAlarmeContext> contextCaptor;

	private BuscarAlarmeUseCaseImpl useCase;
	private Alarme alarme;

	@BeforeEach
	void setUp() {
		useCase = new BuscarAlarmeUseCaseImpl(alarmeRepositoryGateway, List.of(regra));
		alarme = umAlarme();
	}

	@Test
	void deveDevolverOAlarmeEncontrado() {
		when(alarmeRepositoryGateway.buscarPorId(ALARME_ID)).thenReturn(Optional.of(alarme));

		assertSame(alarme, useCase.executar(ALARME_ID));
	}

	@Test
	void devePassarAlarmeNuloAsRegrasQuandoNaoExiste() {
		when(alarmeRepositoryGateway.buscarPorId(ALARME_ID)).thenReturn(Optional.empty());
		doThrow(new AlarmeNaoEncontradoException(ALARME_ID)).when(regra).validar(any());

		assertThrows(AlarmeNaoEncontradoException.class, () -> useCase.executar(ALARME_ID));

		verify(regra).validar(contextCaptor.capture());
		assertNull(contextCaptor.getValue().alarme());
	}

	@Test
	void naoDeveConsultarOGatewayQuandoOIdENulo() {
		doThrow(new AlarmeNaoEncontradoException(ALARME_ID)).when(regra).validar(any());

		assertThrows(AlarmeNaoEncontradoException.class, () -> useCase.executar(null));

		verifyNoInteractions(alarmeRepositoryGateway);
	}
}
