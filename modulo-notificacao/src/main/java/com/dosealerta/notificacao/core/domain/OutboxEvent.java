package com.dosealerta.notificacao.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa o comando pendente de publicação (EnviarMensagemCommand ou EnviarLigacaoCommand,
 * conforme o {@link Canal} resolvido) até que o modulo-mensageria seja efetivamente acionado.
 */
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
		Instant publicadoEm) {

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
				null);
	}

	public OutboxEvent publicado(Instant quando) {
		return new OutboxEvent(
				id, alarmeId, pacienteId, telefone, medicamento, dose, etapa, canal, StatusOutboxEvent.PUBLICADO, criadoEm, quando);
	}
}
