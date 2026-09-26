package com.dosealerta.ia.infra.client;

import java.util.List;

record ReceitaExtraidaIA(
		boolean receitaMedica,
		String nomePrescritor,
		String registroProfissional,
		List<MedicamentoIA> medicamentos) {

	record MedicamentoIA(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {}
}
