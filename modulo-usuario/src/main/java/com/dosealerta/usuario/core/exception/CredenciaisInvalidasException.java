package com.dosealerta.usuario.core.exception;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("Telefone ou senha inválidos");
	}
}
