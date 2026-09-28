package com.dosealerta.usuario.core.exception;

public class NumeroInscricaoSusNaoEncontradoException extends RuntimeException {

	public NumeroInscricaoSusNaoEncontradoException(String numeroInscricaoSus) {
		super("Número de inscrição do SUS não encontrado: " + numeroInscricaoSus);
	}
}
