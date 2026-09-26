package com.dosealerta.ia.core.dto;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * {@code camposPendentes} lista o que a receita não trouxe e o paciente precisa informar para confirmar
 * (subconjunto de dose, frequenciaHoras, duracaoDias).
 */
public record ReceitaOutput(
		UUID id,
		UUID pacienteId,
		String medicamento,
		String dose,
		Integer frequenciaHoras,
		Integer duracaoDias,
		Instant horarioInicial,
		StatusReceita status,
		List<String> camposPendentes) {

	public static ReceitaOutput de(Receita receita) {
		return new ReceitaOutput(
				receita.getId(),
				receita.getPacienteId(),
				receita.getMedicamento(),
				receita.getDose(),
				receita.getFrequenciaHoras(),
				receita.getDuracaoDias(),
				receita.getHorarioInicial(),
				receita.getStatus(),
				receita.camposPendentes());
	}
}
