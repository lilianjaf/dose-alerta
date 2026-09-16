package com.dosealerta.notificacao.core.usecase;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SolicitarEnvioUseCaseTest {

	@Mock
	private EstrategiaCanalGateway estrategiaCanalGateway;

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	private SolicitarEnvioUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new SolicitarEnvioUseCase(estrategiaCanalGateway, outboxEventRepositoryGateway);
	}

	@Test
	void deveResolverCanalEGravarEventoDeOutboxPendente() {
		var input = new SolicitarEnvioInput(
				UUID.randomUUID(), UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", EtapaEscalonamento.LEMBRETE_INICIAL);
		when(estrategiaCanalGateway.resolverCanal(EtapaEscalonamento.LEMBRETE_INICIAL)).thenReturn(Canal.MENSAGEM);
		when(outboxEventRepositoryGateway.salvar(argThat(e -> true))).thenAnswer(inv -> inv.getArgument(0));

		useCase.executar(input);

		verify(outboxEventRepositoryGateway).salvar(argThat((OutboxEvent evento) ->
				evento.alarmeId().equals(input.alarmeId())
						&& evento.pacienteId().equals(input.pacienteId())
						&& evento.telefone().equals(input.telefone())
						&& evento.medicamento().equals(input.medicamento())
						&& evento.dose().equals(input.dose())
						&& evento.etapa() == input.etapa()
						&& evento.canal() == Canal.MENSAGEM));
	}
}
