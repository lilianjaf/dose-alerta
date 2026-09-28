package com.dosealerta.usuario.core.exception;

public class PacienteNaoEncontradoException extends RuntimeException {

	public PacienteNaoEncontradoException(String telefone) {
		super("Paciente não encontrado para o telefone: " + telefone);
	}
}
