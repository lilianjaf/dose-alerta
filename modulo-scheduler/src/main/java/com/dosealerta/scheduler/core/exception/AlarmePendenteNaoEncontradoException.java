package com.dosealerta.scheduler.core.exception;

public class AlarmePendenteNaoEncontradoException extends RuntimeException {

	public AlarmePendenteNaoEncontradoException(String telefone) {
		super("Nenhum alarme pendente aguardando resposta para o telefone: " + telefone);
	}
}
