package com.dosealerta.scheduler.core.exception;

import java.util.UUID;

public class AlarmeNaoEncontradoException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Alarme não encontrado: ";

	public AlarmeNaoEncontradoException(UUID alarmeId) {
		super(MENSAGEM_PREFIXO + alarmeId);
	}
}
