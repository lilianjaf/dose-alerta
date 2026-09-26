package com.dosealerta.ia.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * Correções opcionais na confirmação. Campo ausente (nulo) significa "manter como foi extraído da receita".
 */
public record ConfirmarReceitaInput(

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

	public static ConfirmarReceitaInput semCorrecoes() {
		return new ConfirmarReceitaInput(null, null, null, null);
	}
}
