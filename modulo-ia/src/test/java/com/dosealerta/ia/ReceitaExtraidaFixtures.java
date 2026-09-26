package com.dosealerta.ia;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import java.util.List;

public final class ReceitaExtraidaFixtures {

	public static final String PRESCRITOR = "Dra. Exemplo";
	public static final String REGISTRO = "CRM 70.760";

	private ReceitaExtraidaFixtures() {
	}

	/** Receita formal, de um prescritor identificado, com um único medicamento. */
	public static ReceitaExtraida umMedicamento(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
		return comMedicamentos(new MedicamentoExtraido(medicamento, dose, frequenciaHoras, duracaoDias));
	}

	public static ReceitaExtraida comMedicamentos(MedicamentoExtraido... medicamentos) {
		return new ReceitaExtraida(true, PRESCRITOR, REGISTRO, List.of(medicamentos));
	}
}
