package com.dosealerta.scheduler.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CriarAlarmeInput(

		@NotNull
		UUID pacienteId,

		@NotBlank
		String medicamento,

		@NotBlank
		String dose,

		@NotNull
		Instant horarioAlvo) {
}
