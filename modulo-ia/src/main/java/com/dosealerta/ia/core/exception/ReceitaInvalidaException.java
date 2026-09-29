package com.dosealerta.ia.core.exception;

public class ReceitaInvalidaException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Extração da receita reprovada no guardrail: ";

	private final String motivo;

	public ReceitaInvalidaException(String motivo) {
		super(MENSAGEM_PREFIXO + motivo);
		this.motivo = motivo;
	}

	public String getMotivo() {
		return motivo;
	}
}
