package com.dosealerta.relatorioadesao.core.dto;

import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record RegistrarInteracaoInput(

		@NotNull
		UUID id,

		@NotNull
		UUID alarmeId,

		@NotNull
		UUID pacienteId,

		@NotBlank
		String medicamento,

		@NotNull
		TipoInteracao tipo,

		@NotNull
		Instant registradaEm) {
}
