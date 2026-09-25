package com.dosealerta.ia.core.exception;

public class ReceitaInvalidaException extends RuntimeException {

	public ReceitaInvalidaException(String motivo) {
		super("Extração da receita reprovada no guardrail: " + motivo);
	}
}
