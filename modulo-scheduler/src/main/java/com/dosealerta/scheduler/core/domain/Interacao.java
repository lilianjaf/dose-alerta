package com.dosealerta.scheduler.core.domain;

import java.time.Instant;
import java.util.UUID;

public record Interacao(UUID id, TipoInteracao tipo, Instant registradaEm) {

	public static Interacao nova(TipoInteracao tipo, Instant quando) {
		return new Interacao(UUID.randomUUID(), tipo, quando);
	}
}
