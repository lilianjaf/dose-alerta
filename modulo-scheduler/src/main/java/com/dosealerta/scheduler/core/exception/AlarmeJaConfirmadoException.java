package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

public class AlarmeJaConfirmadoException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Alarme já confirmado: ";

	public AlarmeJaConfirmadoException(UUID alarmeId) {
		super(MENSAGEM_PREFIXO + alarmeId);
	}
}
