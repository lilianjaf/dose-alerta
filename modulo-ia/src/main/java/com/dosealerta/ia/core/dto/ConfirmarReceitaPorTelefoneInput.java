package com.dosealerta.ia.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarReceitaPorTelefoneInput(

		@NotBlank
		@Pattern(regexp = "^\\+[0-9]{10,15}$", message = "telefone deve estar em formato E.164, ex: +5511999999999")
		String telefone,

		@Pattern(regexp = ".*\\S.*", message = "não pode ser vazio quando informado")
		String medicamento,

		@Pattern(regexp = ".*\\S.*", message = "não pode ser vazio quando informado")
		String dose,

		@Min(1)
		@Max(168)
		Integer frequenciaHoras,

		@Min(1)
		@Max(365)
		Integer duracaoDias) {

	public ConfirmarReceitaInput paraCorrecoes() {
		return new ConfirmarReceitaInput(medicamento, dose, frequenciaHoras, duracaoDias);
	}
}
