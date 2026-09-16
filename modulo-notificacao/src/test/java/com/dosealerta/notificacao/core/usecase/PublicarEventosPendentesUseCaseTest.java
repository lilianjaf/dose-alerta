package com.dosealerta.notificacao.core.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.exception.MensageriaIndisponivelException;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
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

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private MensageriaClientGateway mensageriaClientGateway;

	private PublicarEventosPendentesUseCase useCase;

	private OutboxEvent eventoPendente() {
		return OutboxEvent.novo(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"+5511999999999",
				"Losartana",
				"50mg",
				EtapaEscalonamento.LEMBRETE_INICIAL,
				Canal.MENSAGEM,
				Instant.now());
	}

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCase(outboxEventRepositoryGateway, mensageriaClientGateway);
	}

	@Test
	void devePublicarEventoPendenteEMarcarComoPublicado() {
		OutboxEvent evento = eventoPendente();
		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));

		useCase.executar();

		verify(mensageriaClientGateway).enviar(evento);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(eq(evento.id()), any(Instant.class));
	}

	@Test
	void deveDeixarEventoPendenteQuandoMensageriaFalha() {
		OutboxEvent evento = eventoPendente();
		when(outboxEventRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		doThrow(new MensageriaIndisponivelException("falhou", null))
				.when(mensageriaClientGateway)
				.enviar(evento);

		useCase.executar();

		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(UUID.class), any(Instant.class));
	}
}
