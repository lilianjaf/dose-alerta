package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Representa o {@code HoraDoAlarmeEvent} pendente de publicação até que o modulo-notificacao
 * seja efetivamente acionado.
 *
 * <p>{@code correlationId} é capturado do MDC no momento da criação (Etapa 9.1) — o mesmo id
 * que {@code CorrelationIdFilter} publicou lá para a requisição/job em curso. Sem um valor no
 * MDC (ex.: o job de escalonamento roda na sua própria thread, sem requisição associada), um
 * novo id é gerado aqui mesmo: cada decisão de escalonamento é, nesse caso, a própria origem
 * causal da cadeia que ela dispara (scheduler → notificacao → mensageria), então merece um id
 * só seu, ainda que não amarrado à requisição que criou o alarme originalmente.
 */
public record OutboxEvent(
		UUID id,
		UUID alarmeId,
		EtapaEscalonamento etapa,
		StatusOutboxEvent status,
		Instant criadoEm,
		Instant publicadoEm,
		String correlationId) {

	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	public static OutboxEvent novo(UUID alarmeId, EtapaEscalonamento etapa, Instant quando) {
		return new OutboxEvent(
				UUID.randomUUID(), alarmeId, etapa, StatusOutboxEvent.PENDENTE, quando, null, capturarCorrelationId());
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(id, alarmeId, etapa, StatusOutboxEvent.PUBLICADO, criadoEm, quando, correlationId);
	}

	private static String capturarCorrelationId() {
		String doContexto = MDC.get(MDC_CORRELATION_ID_KEY);
		return doContexto != null ? doContexto : UUID.randomUUID().toString();
	}
}
