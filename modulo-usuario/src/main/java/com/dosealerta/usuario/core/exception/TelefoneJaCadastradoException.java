package com.dosealerta.usuario.core.exception;

public class TelefoneJaCadastradoException extends RuntimeException {

	public TelefoneJaCadastradoException(String telefone) {
		super("Telefone já cadastrado: " + telefone);
	}
}
