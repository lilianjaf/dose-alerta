package com.dosealerta.ia.core.exception;

public class ReceitaIdObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O identificador da receita é obrigatório";

	public ReceitaIdObrigatorioException() {
		super(MENSAGEM);
	}
}
