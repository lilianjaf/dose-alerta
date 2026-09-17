package com.dosealerta.ia.core.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.core.domain.OutboxEvent;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
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
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private SchedulerClientGateway schedulerClientGateway;

	private PublicarEventosPendentesUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCase(
				receitaRepositoryGateway, outboxEventRepositoryGateway, schedulerClientGateway);
	}

	@Test
	void deveAcionarOSchedulerEMarcarOEventoComoPublicado() {
		Instant horarioInicial = Instant.now();
		Receita receita = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, horarioInicial);
		receita.confirmar("Losartana", "50mg", 24, 30, Instant.now());
		OutboxEvent evento = receita.getEventosOutbox().get(0);

		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));

		useCase.executar();

		verify(schedulerClientGateway)
				.criarAlarme(receita.getPacienteId(), receita.getTelefone(), "Losartana", "50mg", horarioInicial);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(eq(evento.id()), any(Instant.class));
	}

	@Test
	void naoDeveMarcarComoPublicadoQuandoSchedulerFalha() {
		Instant horarioInicial = Instant.now();
		Receita receita = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, horarioInicial);
		receita.confirmar("Losartana", "50mg", 24, 30, Instant.now());
		OutboxEvent evento = receita.getEventosOutbox().get(0);

		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));
		doThrow(new RuntimeException("indisponível"))
				.when(schedulerClientGateway)
				.criarAlarme(any(), any(), any(), any(), any());

		useCase.executar();

		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(), any());
	}

	@Test
	void naoDeveFazerNadaQuandoReceitaNaoEEncontrada() {
		OutboxEvent evento = OutboxEvent.novo(UUID.randomUUID(), Instant.now());
		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		when(receitaRepositoryGateway.buscarPorId(evento.receitaId())).thenReturn(Optional.empty());

		useCase.executar();

		verify(schedulerClientGateway, never()).criarAlarme(any(), any(), any(), any(), any());
		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(), any());
	}
}
