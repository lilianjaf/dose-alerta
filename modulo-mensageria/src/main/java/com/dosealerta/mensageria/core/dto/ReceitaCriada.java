package com.dosealerta.mensageria.core.dto;

import java.util.List;

public record ReceitaCriada(
		String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias, List<String> camposPendentes) {

	public boolean completa() {
		return camposPendentes.isEmpty();
	}
}
