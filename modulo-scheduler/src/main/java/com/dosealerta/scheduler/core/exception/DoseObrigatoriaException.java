package com.dosealerta.scheduler.core.exception;

public class DoseObrigatoriaException extends RuntimeException {

	private static final String MENSAGEM = "A dose é obrigatória";

	public DoseObrigatoriaException() {
		super(MENSAGEM);
	}
}
