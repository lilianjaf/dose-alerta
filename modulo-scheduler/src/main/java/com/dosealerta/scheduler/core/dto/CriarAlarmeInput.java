package com.dosealerta.scheduler.core.dto;

import java.time.Instant;
import java.util.UUID;

public record CriarAlarmeInput(
		UUID pacienteId,
		String telefone,
		String medicamento,
		String dose,
		Instant horarioAlvo) {
}
