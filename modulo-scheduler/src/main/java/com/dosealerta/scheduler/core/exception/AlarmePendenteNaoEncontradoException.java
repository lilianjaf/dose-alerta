package com.dosealerta.scheduler.core.exception;

public class AlarmePendenteNaoEncontradoException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Nenhum alarme pendente aguardando resposta para o telefone: ";

	public AlarmePendenteNaoEncontradoException(String telefone) {
		super(MENSAGEM_PREFIXO + telefone);
	}
}
