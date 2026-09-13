package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

public class AlarmeJaConfirmadoException extends RuntimeException {

	public AlarmeJaConfirmadoException(UUID alarmeId) {
		super("Alarme já confirmado: " + alarmeId);
	}
}
