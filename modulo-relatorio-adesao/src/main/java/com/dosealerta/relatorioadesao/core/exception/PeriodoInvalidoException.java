package com.dosealerta.relatorioadesao.core.exception;

import java.time.Instant;

public class PeriodoInvalidoException extends RuntimeException {

	private static final String MENSAGEM = "O início do período (%s) não pode ser posterior ao fim (%s)";

	public PeriodoInvalidoException(Instant inicio, Instant fim) {
		super(MENSAGEM.formatted(inicio, fim));
	}
}
