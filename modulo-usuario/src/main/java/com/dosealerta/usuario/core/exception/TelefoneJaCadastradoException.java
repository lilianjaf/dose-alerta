package com.dosealerta.usuario.core.exception;

public class TelefoneJaCadastradoException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Telefone já cadastrado: ";

	public TelefoneJaCadastradoException(String telefone) {
		super(MENSAGEM_PREFIXO + telefone);
	}
}
