package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.OutboxEvent;
import com.dosealerta.ia.core.gateway.CorrelacaoGateway;
import com.dosealerta.ia.core.gateway.LogGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.time.Clock;

public class PublicarEventosPendentesUseCaseImpl implements PublicarEventosPendentesUseCase {

	private static final int TAMANHO_LOTE = 50;
	private static final String MENSAGEM_FALHA_PUBLICACAO =
			"Falha ao publicar o evento de outbox {}, será retentado no próximo ciclo";

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final SchedulerClientGateway schedulerClientGateway;
	private final LogGateway logGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;

	public PublicarEventosPendentesUseCaseImpl(ReceitaRepositoryGateway receitaRepositoryGateway, OutboxEventRepositoryGateway outboxEventRepositoryGateway, SchedulerClientGateway schedulerClientGateway, LogGateway logGateway, CorrelacaoGateway correlacaoGateway, Clock clock) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.schedulerClientGateway = schedulerClientGateway;
		this.logGateway = logGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
	}

	@Override
	public void executar() {
		outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE).forEach(evento ->
				correlacaoGateway.executarCom(evento.correlationId(), () -> publicarComTolerancia(evento)));
	}

	private void publicarComTolerancia(OutboxEvent evento) {
		try {
			publicar(evento);
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_PUBLICACAO, evento.id(), e);
		}
	}

	private void publicar(OutboxEvent evento) {
		receitaRepositoryGateway.buscarPorId(evento.receitaId()).ifPresent(receita -> {
			schedulerClientGateway.criarAlarme(
					receita.getPacienteId(),
					receita.getTelefone(),
					receita.getMedicamento(),
					receita.getDose(),
					receita.getHorarioInicial());
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), clock.instant());
		});
	}
}
