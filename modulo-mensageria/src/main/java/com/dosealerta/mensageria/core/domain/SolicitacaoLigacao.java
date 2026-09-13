package com.dosealerta.mensageria.core.domain;

public record SolicitacaoLigacao(String textoFalado) {

	public static final String DIGITO_CONFIRMACAO = "1";

	public SolicitacaoLigacao {
		if (textoFalado == null || textoFalado.isBlank()) {
			throw new IllegalArgumentException("Texto a ser falado não pode ser vazio");
		}
	}
}
