package com.dosealerta.mensageria.infra.client;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Espelha `ExtracaoOutput` do modulo-ia por completo, mesmo só usando medicamento/camposPendentes/motivo aqui. */
record ExtracaoResponse(List<ReceitaResponse> receitas, List<MedicamentoNaoProcessadoResponse> naoProcessados) {

	record ReceitaResponse(
			UUID id,
			UUID pacienteId,
			String medicamento,
			String dose,
			Integer frequenciaHoras,
			Integer duracaoDias,
			Instant horarioInicial,
			String status,
			List<String> camposPendentes) {}

	record MedicamentoNaoProcessadoResponse(String medicamento, String motivo) {}
}
