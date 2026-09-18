package com.dosealerta.scheduler.core.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicarEventosInteracaoPendentesUseCaseTest {

	@Mock
	private EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway;

	@Mock
	private RelatorioAdesaoClientGateway relatorioAdesaoClientGateway;

	private PublicarEventosInteracaoPendentesUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosInteracaoPendentesUseCase(
				eventoInteracaoRepositoryGateway, relatorioAdesaoClientGateway);
	}

	@Test
	void deveAcionarORelatorioAdesaoEMarcarOEventoComoPublicado() {
		EventoInteracao evento = EventoInteracao.novo(
				UUID.randomUUID(), UUID.randomUUID(), "Losartana", TipoInteracao.CONFIRMACAO, Instant.now());
		when(eventoInteracaoRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));

		useCase.executar();

		verify(relatorioAdesaoClientGateway).registrarInteracao(evento);
		verify(eventoInteracaoRepositoryGateway).marcarComoPublicado(eq(evento.id()), any(Instant.class));
	}

	@Test
	void naoDeveMarcarComoPublicadoQuandoRelatorioAdesaoFalha() {
		EventoInteracao evento = EventoInteracao.novo(
				UUID.randomUUID(), UUID.randomUUID(), "Losartana", TipoInteracao.CONFIRMACAO, Instant.now());
		when(eventoInteracaoRepositoryGateway.buscarPendentes(50)).thenReturn(List.of(evento));
		doThrow(new RuntimeException("indisponível")).when(relatorioAdesaoClientGateway).registrarInteracao(evento);

		useCase.executar();

		verify(eventoInteracaoRepositoryGateway, never()).marcarComoPublicado(any(), any());
	}
}
