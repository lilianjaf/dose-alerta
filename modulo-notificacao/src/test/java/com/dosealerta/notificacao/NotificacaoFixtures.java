package com.dosealerta.notificacao;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public final class NotificacaoFixtures {

	public static final Instant INSTANTE_FIXO = Instant.parse("2026-09-26T20:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, ZoneId.of("America/Sao_Paulo"));
	public static final UUID ALARME_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final UUID OUTRO_ALARME_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
	public static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final String TELEFONE = "+5511999999999";
	public static final String MEDICAMENTO = "Losartana";
	public static final String DOSE = "50mg";
	public static final String CORRELATION_ID = "correlacao-fixa";
	public static final String VALOR_EM_BRANCO = "  ";

	private NotificacaoFixtures() {
	}

	public static SolicitarEnvioInput umaSolicitacaoValida() {
		return new SolicitarEnvioInput(
				ALARME_ID, PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, EtapaEscalonamento.LEMBRETE_INICIAL);
	}

	public static SolicitarEnvioInput umaSolicitacaoCom(
			UUID alarmeId, UUID pacienteId, String telefone, String medicamento, String dose, EtapaEscalonamento etapa) {
		return new SolicitarEnvioInput(alarmeId, pacienteId, telefone, medicamento, dose, etapa);
	}

	public static OutboxEvent umEventoOutbox() {
		return umEventoOutbox(ALARME_ID, Canal.MENSAGEM, EtapaEscalonamento.LEMBRETE_INICIAL, INSTANTE_FIXO);
	}

	public static OutboxEvent umEventoOutbox(UUID alarmeId) {
		return umEventoOutbox(alarmeId, Canal.MENSAGEM, EtapaEscalonamento.LEMBRETE_INICIAL, INSTANTE_FIXO);
	}

	public static OutboxEvent umEventoOutbox(Canal canal, EtapaEscalonamento etapa) {
		return umEventoOutbox(ALARME_ID, canal, etapa, INSTANTE_FIXO);
	}

	public static OutboxEvent umEventoOutboxCriadoEm(Instant criadoEm) {
		return umEventoOutbox(ALARME_ID, Canal.MENSAGEM, EtapaEscalonamento.LEMBRETE_INICIAL, criadoEm);
	}

	public static OutboxEvent umEventoOutbox(UUID alarmeId, Canal canal, EtapaEscalonamento etapa, Instant criadoEm) {
		return OutboxEvent.novo(
				alarmeId, PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, etapa, canal, criadoEm, CORRELATION_ID);
	}

	public static OutboxEvent comTentativas(OutboxEvent base, int tentativas) {
		return new OutboxEvent(
				base.id(), base.alarmeId(), base.pacienteId(), base.telefone(), base.medicamento(), base.dose(),
				base.etapa(), base.canal(), base.status(), base.criadoEm(), null, base.correlationId(), tentativas,
				null);
	}
}
