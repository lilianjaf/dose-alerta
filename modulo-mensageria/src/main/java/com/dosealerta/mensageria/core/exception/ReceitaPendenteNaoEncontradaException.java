package com.dosealerta.mensageria.core.exception;

public class ReceitaPendenteNaoEncontradaException extends RuntimeException {

	public ReceitaPendenteNaoEncontradaException(String telefone) {
		super("Nenhuma receita aguardando confirmação para o telefone: " + telefone);
	}
}
