package com.dosealerta.mensageria.core.domain;

public record SolicitacaoLigacao(String textoFalado) {

	private static final String MENSAGEM_TEXTO_FALADO_VAZIO = "Texto a ser falado não pode ser vazio";
	public static final String DIGITO_CONFIRMACAO = "1";

	public SolicitacaoLigacao {
		if (textoFalado == null || textoFalado.isBlank()) {
			throw new IllegalArgumentException(MENSAGEM_TEXTO_FALADO_VAZIO);
		}
	}
}
