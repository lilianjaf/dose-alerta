package com.dosealerta.notificacao.core.exception;

public class PacienteIdObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O identificador do paciente é obrigatório";

	public PacienteIdObrigatorioException() {
		super(MENSAGEM);
	}
}
