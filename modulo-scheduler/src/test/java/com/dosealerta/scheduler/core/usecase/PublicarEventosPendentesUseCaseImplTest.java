package com.dosealerta.scheduler.core.usecase;

import static com.dosealerta.scheduler.SchedulerFixtures.CLOCK_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.CORRELATION_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static com.dosealerta.scheduler.SchedulerFixtures.umEventoOutbox;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.exception.NotificacaoIndisponivelException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PublicarEventosPendentesUseCaseImplTest extends TesteUnitarioBase {

	private static final int TAMANHO_LOTE = 50;

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private NotificacaoClientGateway notificacaoClientGateway;

	@Mock
	private LogGateway logGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	private PublicarEventosPendentesUseCaseImpl useCase;
	private Alarme alarme;
	private OutboxEvent evento;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCaseImpl(
				alarmeRepositoryGateway,
				outboxEventRepositoryGateway,
				notificacaoClientGateway,
				logGateway,
				correlacaoGateway,
				CLOCK_FIXO);
		alarme = umAlarme();
		evento = umEventoOutbox(alarme.getId());
		when(outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE)).thenReturn(List.of(evento));
		doAnswer(inv -> {
					((Runnable) inv.getArgument(1)).run();
					return null;
				})
				.when(correlacaoGateway)
				.executarCom(eq(CORRELATION_ID), any(Runnable.class));
	}

	@Test
	void devePublicarEventoPendenteEMarcarComoPublicadoComDataDoRelogio() {
		when(alarmeRepositoryGateway.buscarPorId(alarme.getId())).thenReturn(Optional.of(alarme));

		useCase.executar();

		verify(notificacaoClientGateway).solicitarEnvio(alarme, EtapaEscalonamento.LEMBRETE_INICIAL);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(evento.id(), INSTANTE_FIXO);
	}

	@Test
	void deveDeixarEventoPendenteERegistrarAvisoQuandoNotificacaoFalha() {
		when(alarmeRepositoryGateway.buscarPorId(alarme.getId())).thenReturn(Optional.of(alarme));
		doThrow(new NotificacaoIndisponivelException("falhou", null))
				.when(notificacaoClientGateway)
				.solicitarEnvio(alarme, EtapaEscalonamento.LEMBRETE_INICIAL);

		useCase.executar();

		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(UUID.class), any());
		verify(logGateway).aviso(any(), eq(evento.id()), any(RuntimeException.class));
	}

	@Test
	void deveIgnorarEventoOrfaoSemAlarmeCorrespondente() {
		when(alarmeRepositoryGateway.buscarPorId(alarme.getId())).thenReturn(Optional.empty());

		useCase.executar();

		verify(notificacaoClientGateway, never()).solicitarEnvio(any(Alarme.class), any(EtapaEscalonamento.class));
	}
}
