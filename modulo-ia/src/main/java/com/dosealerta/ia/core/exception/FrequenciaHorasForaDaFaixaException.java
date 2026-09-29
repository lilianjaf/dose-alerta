package com.dosealerta.ia.core.exception;

public class FrequenciaHorasForaDaFaixaException extends RuntimeException {

	private static final String MENSAGEM = "frequenciaHoras deve estar entre 1 e 168";

	public FrequenciaHorasForaDaFaixaException() {
		super(MENSAGEM);
	}
}
