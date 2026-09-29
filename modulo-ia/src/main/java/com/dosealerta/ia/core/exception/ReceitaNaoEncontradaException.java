package com.dosealerta.ia.core.exception;

import java.util.UUID;

public class ReceitaNaoEncontradaException extends RuntimeException {

	private static final String MENSAGEM_POR_ID = "Receita não encontrada: ";
	private static final String MENSAGEM_POR_TELEFONE = "Nenhuma receita aguardando confirmação para o telefone: ";

	public ReceitaNaoEncontradaException(UUID id) {
		super(MENSAGEM_POR_ID + id);
	}

	public ReceitaNaoEncontradaException(String telefone) {
		super(MENSAGEM_POR_TELEFONE + telefone);
	}
}
