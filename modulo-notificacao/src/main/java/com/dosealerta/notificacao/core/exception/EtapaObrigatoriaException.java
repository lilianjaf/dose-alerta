package com.dosealerta.notificacao.core.exception;

public class EtapaObrigatoriaException extends RuntimeException {

	private static final String MENSAGEM = "A etapa do escalonamento é obrigatória";

	public EtapaObrigatoriaException() {
		super(MENSAGEM);
	}
}
