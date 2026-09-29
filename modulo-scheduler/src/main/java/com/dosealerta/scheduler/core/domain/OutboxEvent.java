package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
		UUID id,
		UUID alarmeId,
		EtapaEscalonamento etapa,
		StatusOutboxEvent status,
		Instant criadoEm,
		Instant publicadoEm,
		String correlationId) {

	public static OutboxEvent novo(UUID alarmeId, EtapaEscalonamento etapa, Instant quando, String correlationId) {
		return new OutboxEvent(
				UUID.randomUUID(), alarmeId, etapa, StatusOutboxEvent.PENDENTE, quando, null, correlationId);
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(id, alarmeId, etapa, StatusOutboxEvent.PUBLICADO, criadoEm, quando, correlationId);
	}
}
