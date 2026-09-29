package com.dosealerta.mensageria.core.exception;

public class AlarmeIndisponivelException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Falha ao comunicar interação do paciente ";
	private static final String MENSAGEM_SUFIXO = " ao modulo-scheduler";

	public AlarmeIndisponivelException(String telefone, Throwable causa) {
		super(MENSAGEM_PREFIXO + telefone + MENSAGEM_SUFIXO, causa);
	}
}
