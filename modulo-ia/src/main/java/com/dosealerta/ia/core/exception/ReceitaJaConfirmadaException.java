package com.dosealerta.ia.core.exception;

import java.util.UUID;

public class ReceitaJaConfirmadaException extends RuntimeException {

	public ReceitaJaConfirmadaException(UUID id) {
		super("Receita já confirmada: " + id);
	}
}
