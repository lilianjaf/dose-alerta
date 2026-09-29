package com.dosealerta.usuario.core.exception;

public class NumeroInscricaoSusFormatoInvalidoException extends RuntimeException {

	private static final String MENSAGEM = "O número de inscrição do SUS deve ter 15 dígitos numéricos (Cartão SUS)";

	public NumeroInscricaoSusFormatoInvalidoException() {
		super(MENSAGEM);
	}
}
