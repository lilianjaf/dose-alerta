package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

public class ConflitoConcorrenciaException extends RuntimeException {

	public ConflitoConcorrenciaException(UUID alarmeId, Throwable causa) {
		super("Conflito de concorrência ao salvar o alarme " + alarmeId, causa);
	}
}
