package com.dosealerta.ia.core.exception;

public class ExtracaoReceitaFalhouException extends RuntimeException {

	public ExtracaoReceitaFalhouException(String mensagem) {
		super(mensagem);
	}

	public ExtracaoReceitaFalhouException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
