package com.dosealerta.ia.core.exception;

public class CampoInformadoEmBrancoException extends RuntimeException {

	private static final String MENSAGEM = "%s não pode ser vazio quando informado";

	public CampoInformadoEmBrancoException(String campo) {
		super(MENSAGEM.formatted(campo));
	}
}
