package com.dosealerta.ia.core.exception;

import java.util.UUID;

public class ReceitaJaConfirmadaException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Receita já confirmada: ";

	public ReceitaJaConfirmadaException(UUID id) {
		super(MENSAGEM_PREFIXO + id);
	}
}
