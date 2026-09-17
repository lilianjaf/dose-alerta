package com.dosealerta.ia.core.exception;

public class SchedulerIndisponivelException extends RuntimeException {

	public SchedulerIndisponivelException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
