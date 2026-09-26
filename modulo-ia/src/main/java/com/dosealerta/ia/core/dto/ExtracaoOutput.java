package com.dosealerta.ia.core.dto;

import java.util.List;

public record ExtracaoOutput(List<ReceitaOutput> receitas, List<MedicamentoNaoProcessado> naoProcessados) {

	public static ExtracaoOutput de(ResultadoExtracao resultado) {
		return new ExtracaoOutput(
				resultado.receitas().stream().map(ReceitaOutput::de).toList(), resultado.naoProcessados());
	}
}
