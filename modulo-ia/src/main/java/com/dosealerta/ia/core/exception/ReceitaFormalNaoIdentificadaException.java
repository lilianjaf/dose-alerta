package com.dosealerta.ia.core.exception;

public class ReceitaFormalNaoIdentificadaException extends RuntimeException {

	public static final String ORIENTACAO =
			"Não foi possível identificar uma receita médica formal, com nome do médico e CRM. "
					+ "Utilize apenas medicamentos devidamente indicados por um médico, mediante receita formal.";

	private final String motivo;

	public ReceitaFormalNaoIdentificadaException(String motivo) {
		super(ORIENTACAO);
		this.motivo = motivo;
	}

	public String getMotivo() {
		return motivo;
	}
}
