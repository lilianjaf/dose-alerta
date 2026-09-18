package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa o evento de interação pendente de publicação ao modulo-relatorio-adesao
 * (Etapa 8) — nomeado como {@code InteracaoRegistradaEvent} no contrato entre módulos. Vive
 * ao lado de {@link OutboxEvent} (não substitui) porque tem um propósito e destino
 * diferentes: {@code OutboxEvent} aciona o modulo-notificacao a cada etapa de escalonamento;
 * este aciona o modulo-relatorio-adesao a cada interação do paciente (confirmação, não
 * confirmação, atendimento de ligação).
 */
public record EventoInteracao(
		UUID id,
		UUID alarmeId,
		UUID pacienteId,
		String medicamento,
		TipoInteracao tipo,
		Instant registradaEm,
		StatusOutboxEvent status,
		Instant publicadoEm) {

	public static EventoInteracao novo(
			UUID alarmeId, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant quando) {
		return new EventoInteracao(
				UUID.randomUUID(), alarmeId, pacienteId, medicamento, tipo, quando, StatusOutboxEvent.PENDENTE, null);
	}

	public EventoInteracao publicado(Instant quando) {
		return new EventoInteracao(
				id, alarmeId, pacienteId, medicamento, tipo, registradaEm, StatusOutboxEvent.PUBLICADO, quando);
	}
}
