package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

public class ConflitoConcorrenciaException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Conflito de concorrência ao salvar o alarme ";

	public ConflitoConcorrenciaException(UUID alarmeId, Throwable causa) {
		super(MENSAGEM_PREFIXO + alarmeId, causa);
	}
}
