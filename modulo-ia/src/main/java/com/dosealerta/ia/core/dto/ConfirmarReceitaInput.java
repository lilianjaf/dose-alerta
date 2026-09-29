package com.dosealerta.ia.core.dto;


public record ConfirmarReceitaInput(
		String medicamento,
		String dose,
		Integer frequenciaHoras,
		Integer duracaoDias) {

	public static ConfirmarReceitaInput semCorrecoes() {
		return new ConfirmarReceitaInput(null, null, null, null);
	}
}
