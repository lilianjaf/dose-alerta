package com.dosealerta.usuario.core.exception;

public class CredenciaisInvalidasException extends RuntimeException {

	private static final String MENSAGEM = "Telefone ou senha inválidos";

	public CredenciaisInvalidasException() {
		super(MENSAGEM);
	}
}
