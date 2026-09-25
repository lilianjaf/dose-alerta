package com.dosealerta.relatorioadesao.core.domain;

import java.time.Instant;
import java.util.UUID;

public record Interacao(UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant registradaEm) {

	public static Interacao nova(UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant quando) {
		return new Interacao(id, pacienteId, medicamento, tipo, quando);
	}
}
