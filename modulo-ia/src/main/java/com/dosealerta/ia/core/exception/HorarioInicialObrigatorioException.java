package com.dosealerta.ia.core.exception;

public class HorarioInicialObrigatorioException extends RuntimeException {

	private static final String MENSAGEM = "O horário inicial da primeira dose é obrigatório";

	public HorarioInicialObrigatorioException() {
		super(MENSAGEM);
	}
}
