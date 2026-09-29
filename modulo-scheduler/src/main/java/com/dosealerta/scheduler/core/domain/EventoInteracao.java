package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;

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

	public static EventoInteracao novo(
			UUID alarmeId, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant quando, String correlationId) {
		return new EventoInteracao(
				UUID.randomUUID(),
				alarmeId,
				pacienteId,
				medicamento,
				tipo,
				quando,
				StatusOutboxEvent.PENDENTE,
				null,
				correlationId);
	}

	public EventoInteracao publicado(Instant quando) {
		return new EventoInteracao(
				id, alarmeId, pacienteId, medicamento, tipo, registradaEm, StatusOutboxEvent.PUBLICADO, quando,
				correlationId);
	}
}
