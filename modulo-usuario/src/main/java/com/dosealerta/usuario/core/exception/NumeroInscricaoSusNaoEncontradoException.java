package com.dosealerta.usuario.core.exception;

public class NumeroInscricaoSusNaoEncontradoException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Número de inscrição do SUS não encontrado: ";

	public NumeroInscricaoSusNaoEncontradoException(String numeroInscricaoSus) {
		super(MENSAGEM_PREFIXO + numeroInscricaoSus);
	}
}
