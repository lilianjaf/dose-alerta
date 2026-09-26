package com.dosealerta.ia.core.dto;

public record ReceitaExtraida(
		String medicamento,
		String dose,
		int frequenciaHoras,
		int duracaoDias,
		boolean receitaMedica,
		String nomeMedico,
		String crm) {
}
