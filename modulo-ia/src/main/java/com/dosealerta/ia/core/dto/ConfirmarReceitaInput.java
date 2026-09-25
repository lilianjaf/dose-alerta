package com.dosealerta.ia.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmarReceitaInput(

		@NotBlank
		String medicamento,

		@NotBlank
		String dose,

		@NotNull
		@Min(1)
		@Max(24)
		Integer frequenciaHoras,

		@NotNull
		@Min(1)
		@Max(365)
		Integer duracaoDias) {
}
