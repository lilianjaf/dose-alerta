package com.dosealerta.usuario.core.exception;

public class NumeroInscricaoSusObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O número de inscrição do SUS é obrigatório";

	public NumeroInscricaoSusObrigatorioException() {
		super(MENSAGEM);
	}
}
