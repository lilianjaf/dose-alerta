package com.dosealerta.relatorioadesao.core.dto;

import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import java.time.Instant;
import java.util.UUID;

public record RegistrarInteracaoInput(
		UUID id,
		UUID alarmeId,
		UUID pacienteId,
		String medicamento,
		TipoInteracao tipo,
		Instant registradaEm) {
}
