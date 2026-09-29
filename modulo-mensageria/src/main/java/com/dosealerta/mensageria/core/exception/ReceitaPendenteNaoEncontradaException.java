package com.dosealerta.mensageria.core.exception;

public class ReceitaPendenteNaoEncontradaException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Nenhuma receita aguardando confirmação para o telefone: ";

	public ReceitaPendenteNaoEncontradaException(String telefone) {
		super(MENSAGEM_PREFIXO + telefone);
	}
}
