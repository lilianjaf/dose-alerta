package com.dosealerta.mensageria.core.exception;

public class LigacaoFalhouException extends RuntimeException {

	public LigacaoFalhouException(String telefone, Throwable causa) {
		super("Falha ao realizar ligação para " + telefone, causa);
	}
}
