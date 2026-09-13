package com.dosealerta.mensageria.core.exception;

public class EnvioMensagemFalhouException extends RuntimeException {

	public EnvioMensagemFalhouException(String telefone, Throwable causa) {
		super("Falha ao enviar mensagem para " + telefone, causa);
	}
}
