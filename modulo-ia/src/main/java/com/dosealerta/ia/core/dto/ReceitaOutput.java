package com.dosealerta.ia.core.dto;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import java.time.Instant;
import java.util.UUID;

public record ReceitaOutput(
		UUID id,
		UUID pacienteId,
		String medicamento,
		String dose,
		int frequenciaHoras,
		int duracaoDias,
		Instant horarioInicial,
		StatusReceita status) {

	public static ReceitaOutput de(Receita receita) {
		return new ReceitaOutput(
				receita.getId(),
				receita.getPacienteId(),
				receita.getMedicamento(),
				receita.getDose(),
				receita.getFrequenciaHoras(),
				receita.getDuracaoDias(),
				receita.getHorarioInicial(),
				receita.getStatus());
	}
}
