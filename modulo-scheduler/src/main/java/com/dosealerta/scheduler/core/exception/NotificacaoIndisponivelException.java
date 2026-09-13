package com.dosealerta.scheduler.core.exception;

public class NotificacaoIndisponivelException extends RuntimeException {

	public NotificacaoIndisponivelException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
