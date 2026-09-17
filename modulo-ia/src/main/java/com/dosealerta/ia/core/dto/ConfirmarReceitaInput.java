package com.dosealerta.ia.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload enviado pelo app ao confirmar a receita — sempre os valores finais que o paciente
 * está confirmando, iguais aos extraídos quando não houve correção. Comparar com o que foi
 * extraído (ver {@link com.dosealerta.ia.core.domain.Receita#difereDe}) é o que alimenta o
 * {@code FeedbackExtracao} da Etapa 7.5.
 */
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
