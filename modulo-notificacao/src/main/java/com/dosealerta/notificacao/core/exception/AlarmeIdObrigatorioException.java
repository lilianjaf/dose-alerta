package com.dosealerta.notificacao.core.exception;

public class AlarmeIdObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O identificador do alarme é obrigatório";

	public AlarmeIdObrigatorioException() {
		super(MENSAGEM);
	}
}
