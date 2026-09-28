package com.dosealerta.mensageria.core.exception;

public class ReceitaIndisponivelException extends RuntimeException {

	public ReceitaIndisponivelException(String telefone, Throwable causa) {
		super("Falha ao chamar o modulo-ia para o telefone " + telefone, causa);
	}
}
