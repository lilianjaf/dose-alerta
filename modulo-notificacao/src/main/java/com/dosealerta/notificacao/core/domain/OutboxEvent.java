package com.dosealerta.notificacao.core.domain;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

public record OutboxEvent(
		UUID id,
		UUID alarmeId,
		UUID pacienteId,
		String telefone,
		String medicamento,
		String dose,
		EtapaEscalonamento etapa,
		Canal canal,
		StatusOutboxEvent status,
		Instant criadoEm,
		Instant publicadoEm,
		String correlationId) {

	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	public static OutboxEvent novo(
			UUID alarmeId,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			EtapaEscalonamento etapa,
			Canal canal,
			Instant quando) {
		return new OutboxEvent(
				UUID.randomUUID(),
				alarmeId,
				pacienteId,
				telefone,
				medicamento,
				dose,
				etapa,
				canal,
				StatusOutboxEvent.PENDENTE,
				quando,
				null,
				capturarCorrelationId());
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(
				id, alarmeId, pacienteId, telefone, medicamento, dose, etapa, canal, StatusOutboxEvent.PUBLICADO, criadoEm,
				quando, correlationId);
	}

	private static String capturarCorrelationId() {
		String doContexto = MDC.get(MDC_CORRELATION_ID_KEY);
		return doContexto != null ? doContexto : UUID.randomUUID().toString();
	}
}
