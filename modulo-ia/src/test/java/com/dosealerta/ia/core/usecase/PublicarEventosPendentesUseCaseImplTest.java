package com.dosealerta.ia.core.usecase;

import static com.dosealerta.ia.IaFixtures.CLOCK_FIXO;
import static com.dosealerta.ia.IaFixtures.CORRELATION_ID;
import static com.dosealerta.ia.IaFixtures.DOSE;
import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.MEDICAMENTO;
import static com.dosealerta.ia.IaFixtures.umaReceitaConfirmada;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.OutboxEvent;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.gateway.CorrelacaoGateway;
import com.dosealerta.ia.core.gateway.LogGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PublicarEventosPendentesUseCaseImplTest extends TesteUnitarioBase {

	private static final int TAMANHO_LOTE = 50;

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private SchedulerClientGateway schedulerClientGateway;

	@Mock
	private LogGateway logGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	private PublicarEventosPendentesUseCaseImpl useCase;
	private Receita receita;
	private OutboxEvent evento;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCaseImpl(
				receitaRepositoryGateway,
				outboxEventRepositoryGateway,
				schedulerClientGateway,
				logGateway,
				correlacaoGateway,
				CLOCK_FIXO);
		receita = umaReceitaConfirmada();
		evento = receita.getEventosOutbox().get(0);
		when(outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE)).thenReturn(List.of(evento));
		doAnswer(inv -> {
					((Runnable) inv.getArgument(1)).run();
					return null;
				})
				.when(correlacaoGateway)
				.executarCom(eq(CORRELATION_ID), any(Runnable.class));
	}

	@Test
	void deveAcionarOSchedulerEMarcarOEventoComoPublicadoComDataDoRelogio() {
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));

		useCase.executar();

		verify(schedulerClientGateway)
				.criarAlarme(receita.getPacienteId(), receita.getTelefone(), MEDICAMENTO, DOSE, INSTANTE_FIXO);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(evento.id(), INSTANTE_FIXO);
	}

	@Test
	void naoDeveMarcarComoPublicadoERegistrarAvisoQuandoSchedulerFalha() {
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));
		doThrow(new RuntimeException("indisponível"))
				.when(schedulerClientGateway)
				.criarAlarme(any(), any(), any(), any(), any());

		useCase.executar();

		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(), any());
		verify(logGateway).aviso(any(), eq(evento.id()), any(RuntimeException.class));
	}

	@Test
	void naoDeveFazerNadaQuandoReceitaNaoEEncontrada() {
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.empty());

		useCase.executar();

		verify(schedulerClientGateway, never()).criarAlarme(any(), any(), any(), any(), any());
		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(), any());
	}
}
