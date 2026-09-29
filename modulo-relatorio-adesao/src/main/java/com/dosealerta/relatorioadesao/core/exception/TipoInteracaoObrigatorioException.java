package com.dosealerta.relatorioadesao.core.exception;

public class TipoInteracaoObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O tipo da interação é obrigatório";

	public TipoInteracaoObrigatorioException() {
		super(MENSAGEM);
	}
}
