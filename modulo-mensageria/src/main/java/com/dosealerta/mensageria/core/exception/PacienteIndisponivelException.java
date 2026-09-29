package com.dosealerta.mensageria.core.exception;

public class PacienteIndisponivelException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Falha ao identificar o paciente de telefone ";

	public PacienteIndisponivelException(String telefone, Throwable causa) {
		super(MENSAGEM_PREFIXO + telefone, causa);
	}
}
