package com.dosealerta.mensageria.core.exception;

public class EnvioMensagemFalhouException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Falha ao enviar mensagem para ";

	public EnvioMensagemFalhouException(String telefone, Throwable causa) {
		super(MENSAGEM_PREFIXO + telefone, causa);
	}
}
