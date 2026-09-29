package com.dosealerta.scheduler.core.exception;

public class MedicamentoObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O medicamento é obrigatório";

	public MedicamentoObrigatorioException() {
		super(MENSAGEM);
	}
}
