package com.dosealerta.notificacao.core.exception;

public class MensageriaIndisponivelException extends RuntimeException {

	public MensageriaIndisponivelException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
