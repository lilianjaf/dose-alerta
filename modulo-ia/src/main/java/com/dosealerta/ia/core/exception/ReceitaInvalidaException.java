package com.dosealerta.ia.core.exception;

public class ReceitaInvalidaException extends RuntimeException {

	private final String motivo;

	public ReceitaInvalidaException(String motivo) {
		super("Extração da receita reprovada no guardrail: " + motivo);
		this.motivo = motivo;
	}

	public String getMotivo() {
		return motivo;
	}
}
