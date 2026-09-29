package com.dosealerta.usuario.core.exception;

public class SenhaObrigatoriaException extends RuntimeException {

	private static final String MENSAGEM = "A senha do paciente é obrigatória";

	public SenhaObrigatoriaException() {
		super(MENSAGEM);
	}
}
