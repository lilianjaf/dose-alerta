package com.dosealerta.scheduler.core.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.exception.NotificacaoIndisponivelException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicarEventosPendentesUseCaseTest {

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private NotificacaoClientGateway notificacaoClientGateway;

	private PublicarEventosPendentesUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCase(
				alarmeRepositoryGateway, outboxEventRepositoryGateway, notificacaoClientGateway);
	}

	@Test
	void devePublicarEventoPendenteEMarcarComoPublicado() {
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now());
		OutboxEvent evento = OutboxEvent.novo(alarme.getId(), EtapaEscalonamento.LEMBRETE_INICIAL, Instant.now());
		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		when(alarmeRepositoryGateway.buscarPorId(alarme.getId())).thenReturn(Optional.of(alarme));

		useCase.executar();

		verify(notificacaoClientGateway).solicitarEnvio(alarme, EtapaEscalonamento.LEMBRETE_INICIAL);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(eq(evento.id()), any(Instant.class));
	}

	@Test
	void deveDeixarEventoPendenteQuandoNotificacaoFalha() {
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now());
		OutboxEvent evento = OutboxEvent.novo(alarme.getId(), EtapaEscalonamento.LEMBRETE_INICIAL, Instant.now());
		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		when(alarmeRepositoryGateway.buscarPorId(alarme.getId())).thenReturn(Optional.of(alarme));
		doThrow(new NotificacaoIndisponivelException("falhou", null))
				.when(notificacaoClientGateway)
				.solicitarEnvio(alarme, EtapaEscalonamento.LEMBRETE_INICIAL);

		useCase.executar();

		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(UUID.class), any(Instant.class));
	}

	@Test
	void deveIgnorarEventoOrfaoSemAlarmeCorrespondente() {
		OutboxEvent evento = OutboxEvent.novo(UUID.randomUUID(), EtapaEscalonamento.LEMBRETE_INICIAL, Instant.now());
		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		when(alarmeRepositoryGateway.buscarPorId(evento.alarmeId())).thenReturn(Optional.empty());

		useCase.executar();

		verify(notificacaoClientGateway, never()).solicitarEnvio(any(Alarme.class), any(EtapaEscalonamento.class));
	}
}
