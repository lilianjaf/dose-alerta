package com.dosealerta.ia.core.dto;


public record ConfirmarReceitaPorTelefoneInput(
		String telefone,
		String medicamento,
		String dose,
		Integer frequenciaHoras,
		Integer duracaoDias) {

	public ConfirmarReceitaInput paraCorrecoes() {
		return new ConfirmarReceitaInput(medicamento, dose, frequenciaHoras, duracaoDias);
	}
}
