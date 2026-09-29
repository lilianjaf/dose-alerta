package com.dosealerta.scheduler.core.usecase;

import static com.dosealerta.scheduler.SchedulerFixtures.CLOCK_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.CORRELATION_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.umEventoInteracao;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PublicarEventosInteracaoPendentesUseCaseImplTest extends TesteUnitarioBase {

	private static final int TAMANHO_LOTE = 50;

	@Mock
	private EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway;

	@Mock
	private RelatorioAdesaoClientGateway relatorioAdesaoClientGateway;

	@Mock
	private LogGateway logGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	private PublicarEventosInteracaoPendentesUseCaseImpl useCase;
	private EventoInteracao evento;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosInteracaoPendentesUseCaseImpl(
				eventoInteracaoRepositoryGateway, relatorioAdesaoClientGateway, logGateway, correlacaoGateway, CLOCK_FIXO);
		evento = umEventoInteracao();
		when(eventoInteracaoRepositoryGateway.buscarPendentes(TAMANHO_LOTE)).thenReturn(List.of(evento));
		doAnswer(inv -> {
					((Runnable) inv.getArgument(1)).run();
					return null;
				})
				.when(correlacaoGateway)
				.executarCom(eq(CORRELATION_ID), any(Runnable.class));
	}

	@Test
	void deveAcionarORelatorioAdesaoEMarcarOEventoComoPublicado() {
		useCase.executar();

		verify(relatorioAdesaoClientGateway).registrarInteracao(evento);
		verify(eventoInteracaoRepositoryGateway).marcarComoPublicado(evento.id(), INSTANTE_FIXO);
	}

	@Test
	void naoDeveMarcarComoPublicadoERegistrarAvisoQuandoRelatorioAdesaoFalha() {
		doThrow(new RuntimeException("indisponível")).when(relatorioAdesaoClientGateway).registrarInteracao(evento);

		useCase.executar();

		verify(eventoInteracaoRepositoryGateway, never()).marcarComoPublicado(any(), any());
		verify(logGateway).aviso(any(), eq(evento.id()), any(RuntimeException.class));
	}
}
