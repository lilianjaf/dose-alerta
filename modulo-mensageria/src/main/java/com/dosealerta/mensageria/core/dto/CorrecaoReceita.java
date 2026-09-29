package com.dosealerta.mensageria.core.dto;

public record CorrecaoReceita(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {

	public static CorrecaoReceita vazia() {
		return new CorrecaoReceita(null, null, null, null);
	}
}
