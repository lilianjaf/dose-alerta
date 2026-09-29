package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.time.Clock;

public class PublicarEventosPendentesUseCaseImpl implements PublicarEventosPendentesUseCase {

	private static final int TAMANHO_LOTE = 50;
	private static final String MENSAGEM_FALHA_PUBLICACAO =
			"Falha ao publicar o evento de outbox {}, será retentado no próximo ciclo";

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final NotificacaoClientGateway notificacaoClientGateway;
	private final LogGateway logGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;

	public PublicarEventosPendentesUseCaseImpl(AlarmeRepositoryGateway alarmeRepositoryGateway, OutboxEventRepositoryGateway outboxEventRepositoryGateway, NotificacaoClientGateway notificacaoClientGateway, LogGateway logGateway, CorrelacaoGateway correlacaoGateway, Clock clock) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.notificacaoClientGateway = notificacaoClientGateway;
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
		alarmeRepositoryGateway.buscarPorId(evento.alarmeId()).ifPresent(alarme -> {
			notificacaoClientGateway.solicitarEnvio(alarme, evento.etapa());
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), clock.instant());
		});
	}
}
