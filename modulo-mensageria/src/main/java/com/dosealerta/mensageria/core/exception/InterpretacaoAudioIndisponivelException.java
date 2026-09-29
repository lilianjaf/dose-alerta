package com.dosealerta.mensageria.core.exception;

public class InterpretacaoAudioIndisponivelException extends RuntimeException {

	private static final String MENSAGEM = "Falha ao interpretar o áudio no modulo-ia";

	public InterpretacaoAudioIndisponivelException(Throwable causa) {
		super(MENSAGEM, causa);
	}
}
