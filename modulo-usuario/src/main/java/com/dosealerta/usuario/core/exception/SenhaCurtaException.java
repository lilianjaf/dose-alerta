package com.dosealerta.usuario.core.exception;

public class SenhaCurtaException extends RuntimeException {

	private static final String MENSAGEM = "A senha deve ter no mínimo 8 caracteres";

	public SenhaCurtaException() {
		super(MENSAGEM);
	}
}
