package com.dosealerta.mensageria.core.dto;

/** Correção opcional enviada ao confirmar uma receita pelo telefone. Campo nulo mantém o que foi extraído. */
public record CorrecaoReceita(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {

	public static CorrecaoReceita vazia() {
		return new CorrecaoReceita(null, null, null, null);
	}
}
