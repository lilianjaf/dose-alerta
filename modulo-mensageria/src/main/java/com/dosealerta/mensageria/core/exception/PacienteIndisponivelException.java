package com.dosealerta.mensageria.core.exception;

public class PacienteIndisponivelException extends RuntimeException {

	public PacienteIndisponivelException(String telefone, Throwable causa) {
		super("Falha ao identificar o paciente de telefone " + telefone, causa);
	}
}
