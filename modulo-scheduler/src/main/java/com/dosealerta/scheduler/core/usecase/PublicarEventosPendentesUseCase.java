package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.exception.NotificacaoIndisponivelException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;

/**
 * Publisher assíncrono do Outbox: lê os eventos pendentes e solicita o envio ao
 * modulo-notificacao. Se a chamada falhar, o evento permanece pendente para nova
 * tentativa no próximo ciclo — nada se perde entre o commit e a publicação.
 */
public class PublicarEventosPendentesUseCase {

	private static final int TAMANHO_LOTE = 50;

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final NotificacaoClientGateway notificacaoClientGateway;

	public PublicarEventosPendentesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			NotificacaoClientGateway notificacaoClientGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.notificacaoClientGateway = notificacaoClientGateway;
	}

	public void executar() {
		for (OutboxEvent evento : outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE)) {
			publicar(evento);
		}
	}

	private void publicar(OutboxEvent evento) {
		alarmeRepositoryGateway.buscarPorId(evento.alarmeId()).ifPresent(alarme -> tentarPublicar(evento, alarme));
	}

	private void tentarPublicar(OutboxEvent evento, Alarme alarme) {
		try {
			notificacaoClientGateway.solicitarEnvio(alarme, evento.etapa());
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());
		} catch (NotificacaoIndisponivelException e) {
			// deixa pendente: o próximo ciclo do publisher tenta novamente
		}
	}
}
