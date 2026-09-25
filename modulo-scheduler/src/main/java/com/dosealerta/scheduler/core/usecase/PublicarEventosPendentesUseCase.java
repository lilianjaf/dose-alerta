package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class PublicarEventosPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(PublicarEventosPendentesUseCase.class);
	private static final int TAMANHO_LOTE = 50;
	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

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

			MDC.put(MDC_CORRELATION_ID_KEY, evento.correlationId());
			try {
				publicar(evento);
			} catch (RuntimeException e) {
				log.warn("Falha ao publicar o evento de outbox {}, será retentado no próximo ciclo", evento.id(), e);
			} finally {
				MDC.remove(MDC_CORRELATION_ID_KEY);
			}
		}
	}

	private void publicar(OutboxEvent evento) {
		alarmeRepositoryGateway.buscarPorId(evento.alarmeId()).ifPresent(alarme -> {
			notificacaoClientGateway.solicitarEnvio(alarme, evento.etapa());
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());
		});
	}
}
