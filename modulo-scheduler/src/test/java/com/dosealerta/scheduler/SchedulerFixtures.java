package com.dosealerta.scheduler;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public final class SchedulerFixtures {

	public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");
	public static final Instant INSTANTE_FIXO = Instant.parse("2026-01-01T12:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, FUSO_HORARIO);
	public static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID OUTRO_PACIENTE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	public static final UUID ALARME_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final String TELEFONE = "+5511999999999";
	public static final String OUTRO_TELEFONE = "+5511988887777";
	public static final String MEDICAMENTO = "Losartana";
	public static final String DOSE = "50mg";
	public static final String CORRELATION_ID = "correlacao-fixa";
	public static final String VALOR_EM_BRANCO = "  ";

	private SchedulerFixtures() {
	}

	public static Clock relogioEm(Instant instante) {
		return Clock.fixed(instante, FUSO_HORARIO);
	}

	public static Alarme umAlarme() {
		return Alarme.criar(PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, INSTANTE_FIXO, INSTANTE_FIXO);
	}

	public static Alarme umAlarmeComMedicamento(String medicamento, String dose) {
		return Alarme.criar(PACIENTE_ID, TELEFONE, medicamento, dose, INSTANTE_FIXO, INSTANTE_FIXO);
	}

	public static Alarme umAlarmeEnviado(EtapaEscalonamento etapa) {
		Alarme alarme = umAlarme();
		alarme.registrarEnvio(etapa, INSTANTE_FIXO, CORRELATION_ID);
		return alarme;
	}

	public static CriarAlarmeInput umaCriacaoValida() {
		return new CriarAlarmeInput(PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, INSTANTE_FIXO);
	}

	public static CriarAlarmeInput umaCriacaoCom(
			UUID pacienteId, String telefone, String medicamento, String dose, Instant horarioAlvo) {
		return new CriarAlarmeInput(pacienteId, telefone, medicamento, dose, horarioAlvo);
	}

	public static OutboxEvent umEventoOutbox(UUID alarmeId) {
		return OutboxEvent.novo(alarmeId, EtapaEscalonamento.LEMBRETE_INICIAL, INSTANTE_FIXO, CORRELATION_ID);
	}

	public static EventoInteracao umEventoInteracao() {
		return EventoInteracao.novo(
				ALARME_ID, PACIENTE_ID, MEDICAMENTO, TipoInteracao.CONFIRMACAO, INSTANTE_FIXO, CORRELATION_ID);
	}
}
