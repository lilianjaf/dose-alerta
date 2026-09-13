package com.dosealerta.scheduler.core.dto;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import java.time.Instant;
import java.util.UUID;

public record AlarmeOutput(
		UUID id,
		UUID pacienteId,
		String medicamento,
		String dose,
		Instant horarioAlvo,
		StatusAlarme status,
		EtapaEscalonamento etapaAtual) {

	public static AlarmeOutput de(Alarme alarme) {
		return new AlarmeOutput(
				alarme.getId(),
				alarme.getPacienteId(),
				alarme.getMedicamento(),
				alarme.getDose(),
				alarme.getHorarioAlvo(),
				alarme.getStatus(),
				alarme.getEtapaAtual());
	}
}
