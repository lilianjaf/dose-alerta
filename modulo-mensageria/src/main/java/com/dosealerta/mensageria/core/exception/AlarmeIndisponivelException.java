package com.dosealerta.mensageria.core.exception;

public class AlarmeIndisponivelException extends RuntimeException {

	public AlarmeIndisponivelException(String telefone, Throwable causa) {
		super("Falha ao comunicar interação do paciente " + telefone + " ao modulo-scheduler", causa);
	}
}
