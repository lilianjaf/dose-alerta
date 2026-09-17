package com.dosealerta.ia.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa o {@code ReceitaConfirmadaEvent} pendente de publicação até que o
 * modulo-scheduler seja efetivamente acionado para criar o alarme da receita.
 */
public record OutboxEvent(
		UUID id, UUID receitaId, StatusOutboxEvent status, Instant criadoEm, Instant publicadoEm) {

	public static OutboxEvent novo(UUID receitaId, Instant quando) {
		return new OutboxEvent(UUID.randomUUID(), receitaId, StatusOutboxEvent.PENDENTE, quando, null);
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(id, receitaId, StatusOutboxEvent.PUBLICADO, criadoEm, quando);
	}
}
