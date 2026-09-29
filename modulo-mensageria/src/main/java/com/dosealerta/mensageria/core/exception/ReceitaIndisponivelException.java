package com.dosealerta.mensageria.core.exception;

public class ReceitaIndisponivelException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Falha ao chamar o modulo-ia para o telefone ";

	public ReceitaIndisponivelException(String telefone, Throwable causa) {
		super(MENSAGEM_PREFIXO + telefone, causa);
	}
}
