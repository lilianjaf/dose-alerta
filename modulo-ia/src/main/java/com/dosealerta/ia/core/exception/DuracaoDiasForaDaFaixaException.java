package com.dosealerta.ia.core.exception;

public class DuracaoDiasForaDaFaixaException extends RuntimeException {

	private static final String MENSAGEM = "duracaoDias deve estar entre 1 e 365";

	public DuracaoDiasForaDaFaixaException() {
		super(MENSAGEM);
	}
}
