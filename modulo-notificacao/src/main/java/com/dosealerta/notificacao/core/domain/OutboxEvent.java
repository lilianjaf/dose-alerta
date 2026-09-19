package com.dosealerta.notificacao.core.domain;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Representa o comando pendente de publicação (EnviarMensagemCommand ou EnviarLigacaoCommand,
 * conforme o {@link Canal} resolvido) até que o modulo-mensageria seja efetivamente acionado.
 *
 * <p>{@code correlationId} é capturado do MDC na criação (Etapa 9.1) — o valor propagado pelo
 * modulo-scheduler via header na chamada de entrada, ou um novo id caso este módulo tenha
 * sido acionado sem um (ex.: chamado diretamente, fora do fluxo normal).
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
