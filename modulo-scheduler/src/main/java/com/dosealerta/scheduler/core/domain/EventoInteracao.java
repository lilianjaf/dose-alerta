package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

public record EventoInteracao(
		UUID id,
		UUID alarmeId,
		UUID pacienteId,
		String medicamento,
		TipoInteracao tipo,
		Instant registradaEm,
		StatusOutboxEvent status,
		Instant publicadoEm,
		String correlationId) {

	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	public static EventoInteracao novo(
			UUID alarmeId, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant quando) {
		return new EventoInteracao(
				UUID.randomUUID(),
				alarmeId,
				pacienteId,
				medicamento,
				tipo,
				quando,
				StatusOutboxEvent.PENDENTE,
				null,
				capturarCorrelationId());
	}

	public EventoInteracao publicado(Instant quando) {
		return new EventoInteracao(
				id, alarmeId, pacienteId, medicamento, tipo, registradaEm, StatusOutboxEvent.PUBLICADO, quando,
				correlationId);
	}

	private static String capturarCorrelationId() {
		String doContexto = MDC.get(MDC_CORRELATION_ID_KEY);
		return doContexto != null ? doContexto : UUID.randomUUID().toString();
	}
}
