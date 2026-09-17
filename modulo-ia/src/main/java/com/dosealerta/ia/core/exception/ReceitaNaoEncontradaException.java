package com.dosealerta.ia.core.exception;

import java.util.UUID;

public class ReceitaNaoEncontradaException extends RuntimeException {

	public ReceitaNaoEncontradaException(UUID id) {
		super("Receita não encontrada: " + id);
	}
}
