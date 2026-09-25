package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

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
