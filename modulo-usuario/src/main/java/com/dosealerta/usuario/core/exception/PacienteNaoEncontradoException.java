package com.dosealerta.usuario.core.exception;

public class PacienteNaoEncontradoException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Paciente não encontrado para o telefone: ";

	public PacienteNaoEncontradoException(String telefone) {
		super(MENSAGEM_PREFIXO + telefone);
	}
}
