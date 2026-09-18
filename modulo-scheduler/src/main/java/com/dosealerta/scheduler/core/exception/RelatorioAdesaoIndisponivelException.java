package com.dosealerta.scheduler.core.exception;

public class RelatorioAdesaoIndisponivelException extends RuntimeException {

	public RelatorioAdesaoIndisponivelException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
