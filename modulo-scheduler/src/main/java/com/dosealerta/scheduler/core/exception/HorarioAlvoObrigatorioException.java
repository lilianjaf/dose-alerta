package com.dosealerta.scheduler.core.exception;

public class HorarioAlvoObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O horário alvo é obrigatório";

	public HorarioAlvoObrigatorioException() {
		super(MENSAGEM);
	}
}
