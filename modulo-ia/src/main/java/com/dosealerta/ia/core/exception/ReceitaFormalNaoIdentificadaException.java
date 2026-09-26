package com.dosealerta.ia.core.exception;

public class ReceitaFormalNaoIdentificadaException extends RuntimeException {

	public static final String ORIENTACAO =
			"Não foi possível identificar uma receita formal, com nome e registro (CRM ou CRO) do profissional "
					+ "que prescreveu. Utilize apenas medicamentos devidamente indicados por um profissional "
					+ "habilitado, mediante receita formal.";

	private final String motivo;

	public ReceitaFormalNaoIdentificadaException(String motivo) {
		super(ORIENTACAO);
		this.motivo = motivo;
	}

	public String getMotivo() {
		return motivo;
	}
}
