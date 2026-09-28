package com.dosealerta.mensageria.core.dto;

import java.util.List;

/**
 * {@code dose}, {@code frequenciaHoras} e {@code duracaoDias} vêm nulos quando estão em {@code camposPendentes}
 * (a receita não trouxe ou não foi legível) — são exibidos ao paciente só quando presentes.
 */
public record ReceitaCriada(
		String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias, List<String> camposPendentes) {

	public boolean completa() {
		return camposPendentes.isEmpty();
	}
}
