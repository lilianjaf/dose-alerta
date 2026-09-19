package com.dosealerta.ia.core.domain;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Representa o {@code ReceitaConfirmadaEvent} pendente de publicação até que o
 * modulo-scheduler seja efetivamente acionado para criar o alarme da receita.
 *
 * <p>{@code correlationId} é capturado do MDC na criação (Etapa 9.1) — o id da requisição
 * `POST /receitas/{id}/confirmar` que originou esta confirmação.
 */
public record OutboxEvent(
		UUID id, UUID receitaId, StatusOutboxEvent status, Instant criadoEm, Instant publicadoEm, String correlationId) {

	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	public static OutboxEvent novo(UUID receitaId, Instant quando) {
		return new OutboxEvent(
				UUID.randomUUID(), receitaId, StatusOutboxEvent.PENDENTE, quando, null, capturarCorrelationId());
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(id, receitaId, StatusOutboxEvent.PUBLICADO, criadoEm, quando, correlationId);
	}

	private static String capturarCorrelationId() {
		String doContexto = MDC.get(MDC_CORRELATION_ID_KEY);
		return doContexto != null ? doContexto : UUID.randomUUID().toString();
	}
}
