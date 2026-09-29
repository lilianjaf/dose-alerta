package com.dosealerta.notificacao.core.domain;

import java.time.Instant;
import java.util.UUID;

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
		String correlationId,
		int tentativas,
		Instant proximaTentativaEm) {

	public static OutboxEvent novo(
			UUID alarmeId,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			EtapaEscalonamento etapa,
			Canal canal,
			Instant quando,
			String correlationId) {
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
				correlationId,
				0,
				null);
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(
				id, alarmeId, pacienteId, telefone, medicamento, dose, etapa, canal, StatusOutboxEvent.PUBLICADO, criadoEm,
				quando, correlationId, tentativas, null);
	}
}
