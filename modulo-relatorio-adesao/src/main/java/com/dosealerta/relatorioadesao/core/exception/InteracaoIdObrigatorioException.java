package com.dosealerta.relatorioadesao.core.exception;

public class InteracaoIdObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O identificador da interação é obrigatório";

	public InteracaoIdObrigatorioException() {
		super(MENSAGEM);
	}
}
