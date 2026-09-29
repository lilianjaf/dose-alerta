package com.dosealerta.notificacao.core.exception;

public class TelefoneObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O telefone do paciente é obrigatório";

	public TelefoneObrigatorioException() {
		super(MENSAGEM);
	}
}
