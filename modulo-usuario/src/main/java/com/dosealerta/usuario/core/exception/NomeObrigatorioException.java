package com.dosealerta.usuario.core.exception;

public class NomeObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O nome do paciente é obrigatório";

	public NomeObrigatorioException() {
		super(MENSAGEM);
	}
}
