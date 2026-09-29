package com.dosealerta.ia.core.dto;

import java.util.List;

public record ReceitaExtraida(
		boolean receitaMedica,
		String nomePrescritor,
		String registroProfissional,
		List<MedicamentoExtraido> medicamentos) {
}
