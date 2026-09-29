package com.dosealerta.scheduler.core.exception;

public class TelefoneFormatoInvalidoException extends RuntimeException {

	private static final String MENSAGEM = "O telefone deve estar em formato E.164, ex: +5511999999999";

	public TelefoneFormatoInvalidoException() {
		super(MENSAGEM);
	}
}
