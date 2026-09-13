package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

public class AlarmeNaoEncontradoException extends RuntimeException {

	public AlarmeNaoEncontradoException(UUID alarmeId) {
		super("Alarme não encontrado: " + alarmeId);
	}
}
