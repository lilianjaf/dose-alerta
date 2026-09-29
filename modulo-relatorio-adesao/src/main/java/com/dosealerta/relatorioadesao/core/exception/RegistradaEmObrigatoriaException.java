package com.dosealerta.relatorioadesao.core.exception;

public class RegistradaEmObrigatoriaException extends RuntimeException {

	private static final String MENSAGEM = "A data de registro da interação é obrigatória";

	public RegistradaEmObrigatoriaException() {
		super(MENSAGEM);
	}
}
