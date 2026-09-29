package com.dosealerta.mensageria.core.exception;

public class LigacaoFalhouException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Falha ao realizar ligação para ";

	public LigacaoFalhouException(String telefone, Throwable causa) {
		super(MENSAGEM_PREFIXO + telefone, causa);
	}
}
