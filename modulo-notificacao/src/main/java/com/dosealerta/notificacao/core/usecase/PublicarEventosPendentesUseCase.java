package com.dosealerta.notificacao.core.usecase;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Publisher assíncrono do Outbox: lê os comandos pendentes e aciona o modulo-mensageria. Se
 * a publicação de um evento falhar por qualquer motivo, ele permanece pendente para nova
 * tentativa no próximo ciclo — nada se perde entre o commit e a publicação, e os demais
 * eventos do lote continuam sendo processados.
 */
public class PublicarEventosPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(PublicarEventosPendentesUseCase.class);
	private static final int TAMANHO_LOTE = 50;
	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final MensageriaClientGateway mensageriaClientGateway;

	public PublicarEventosPendentesUseCase(
			OutboxEventRepositoryGateway outboxEventRepositoryGateway, MensageriaClientGateway mensageriaClientGateway) {
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.mensageriaClientGateway = mensageriaClientGateway;
	}

	public void executar() {
		for (OutboxEvent evento : outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE)) {
			MDC.put(MDC_CORRELATION_ID_KEY, evento.correlationId());
			try {
				mensageriaClientGateway.enviar(evento);
				outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());
			} catch (RuntimeException e) {
				log.warn("Falha ao publicar o evento de outbox {}, será retentado no próximo ciclo", evento.id(), e);
			} finally {
				MDC.remove(MDC_CORRELATION_ID_KEY);
			}
		}
	}
}
