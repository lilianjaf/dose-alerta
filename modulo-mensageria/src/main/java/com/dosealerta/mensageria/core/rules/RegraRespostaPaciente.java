package com.dosealerta.mensageria.core.rules;

public final class RegraRespostaPaciente {

	public static final String TEXTO_CONFIRMACAO = "CONFIRMAR";

	private RegraRespostaPaciente() {
	}

	public static boolean ehConfirmacao(String corpo, String textoBotao) {
		return TEXTO_CONFIRMACAO.equalsIgnoreCase(normalizar(textoBotao))
				|| TEXTO_CONFIRMACAO.equalsIgnoreCase(normalizar(corpo));
	}

	private static String normalizar(String texto) {
		return texto == null ? "" : texto.trim();
	}
}
