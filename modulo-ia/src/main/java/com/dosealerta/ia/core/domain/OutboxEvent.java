package com.dosealerta.ia.core.domain;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
		UUID id, UUID receitaId, StatusOutboxEvent status, Instant criadoEm, Instant publicadoEm, String correlationId) {

	public static OutboxEvent novo(UUID receitaId, Instant quando, String correlationId) {
		return new OutboxEvent(
				UUID.randomUUID(), receitaId, StatusOutboxEvent.PENDENTE, quando, null, correlationId);
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(id, receitaId, StatusOutboxEvent.PUBLICADO, criadoEm, quando, correlationId);
	}
}
