package com.dosealerta.notificacao.core.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.core.exception.MensageriaIndisponivelException;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicarEventosPendentesUseCaseTest {

	private static final Instant AGORA = Instant.parse("2026-09-26T20:00:00Z");

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private MensageriaClientGateway mensageriaClientGateway;

	private PublicarEventosPendentesUseCase useCase;

	private OutboxEvent eventoPendente(Instant criadoEm, int tentativas) {
		OutboxEvent novo = OutboxEvent.novo(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"+5511999999999",
				"Losartana",
				"50mg",
				EtapaEscalonamento.LEMBRETE_INICIAL,
				Canal.MENSAGEM,
				criadoEm);
		return new OutboxEvent(
				novo.id(), novo.alarmeId(), novo.pacienteId(), novo.telefone(), novo.medicamento(), novo.dose(),
				novo.etapa(), novo.canal(), StatusOutboxEvent.PENDENTE, criadoEm, null, novo.correlationId(), tentativas,
				null);
	}

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCase(outboxEventRepositoryGateway, mensageriaClientGateway);
	}

	@Test
	void devePublicarEventoPendenteEMarcarComoPublicado() {
		OutboxEvent evento = eventoPendente(AGORA.minusSeconds(10), 0);
		when(outboxEventRepositoryGateway.buscarPendentes(50, AGORA)).thenReturn(List.of(evento));

		useCase.executar(AGORA);

		verify(mensageriaClientGateway).enviar(evento);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(evento.id(), AGORA);
	}

	@Test
	void deveAgendarNovaTentativaComEsperaCrescenteQuandoAMensageriaFalha() {
		OutboxEvent primeiraFalha = eventoPendente(AGORA.minusSeconds(10), 0);
		OutboxEvent terceiraFalha = eventoPendente(AGORA.minusSeconds(10), 2);
		when(outboxEventRepositoryGateway.buscarPendentes(50, AGORA)).thenReturn(List.of(primeiraFalha, terceiraFalha));
		doThrow(new MensageriaIndisponivelException("falhou", null)).when(mensageriaClientGateway).enviar(any());

		useCase.executar(AGORA);

		verify(outboxEventRepositoryGateway).registrarFalha(primeiraFalha.id(), 1, AGORA.plus(Duration.ofSeconds(30)));
		verify(outboxEventRepositoryGateway).registrarFalha(terceiraFalha.id(), 3, AGORA.plus(Duration.ofMinutes(2)));
		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(UUID.class), any(Instant.class));
		verify(outboxEventRepositoryGateway, never()).marcarComoFalhou(any(UUID.class), anyInt());
	}

	@Test
	void devePararDeTentarQuandoEsgotaAsTentativas() {
		OutboxEvent ultimaChance = eventoPendente(AGORA.minusSeconds(10), PublicarEventosPendentesUseCase.MAX_TENTATIVAS - 1);
		when(outboxEventRepositoryGateway.buscarPendentes(50, AGORA)).thenReturn(List.of(ultimaChance));
		doThrow(new MensageriaIndisponivelException("falhou", null)).when(mensageriaClientGateway).enviar(ultimaChance);

		useCase.executar(AGORA);

		verify(outboxEventRepositoryGateway)
				.marcarComoFalhou(ultimaChance.id(), PublicarEventosPendentesUseCase.MAX_TENTATIVAS);
		verify(outboxEventRepositoryGateway, never()).registrarFalha(any(UUID.class), anyInt(), any(Instant.class));
	}

	@Test
	void deveDescartarSemEnviarOEventoVelhoDemais() {
		OutboxEvent velho = eventoPendente(AGORA.minus(Duration.ofHours(3)), 0);
		when(outboxEventRepositoryGateway.buscarPendentes(50, AGORA)).thenReturn(List.of(velho));

		useCase.executar(AGORA);

		verify(mensageriaClientGateway, never()).enviar(any());
		verify(outboxEventRepositoryGateway).marcarComoExpirado(velho.id());
		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(eq(velho.id()), any(Instant.class));
	}

	@Test
	void naoDeveExpirarEventoDentroDaValidade() {
		OutboxEvent recente = eventoPendente(AGORA.minus(Duration.ofMinutes(29)), 0);
		when(outboxEventRepositoryGateway.buscarPendentes(50, AGORA)).thenReturn(List.of(recente));

		useCase.executar(AGORA);

		verify(mensageriaClientGateway).enviar(recente);
		verify(outboxEventRepositoryGateway, never()).marcarComoExpirado(any(UUID.class));
	}
}
