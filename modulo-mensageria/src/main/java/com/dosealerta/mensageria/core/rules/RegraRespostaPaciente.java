package com.dosealerta.mensageria.core.rules;

public final class RegraRespostaPaciente {

	public static final String TEXTO_CONFIRMACAO = "CONFIRMAR";

	private RegraRespostaPaciente() {
	}

	public static boolean ehConfirmacao(String corpo, String textoBotao) {
		return TEXTO_CONFIRMACAO.equalsIgnoreCase(normalizar(textoBotao))
				|| TEXTO_CONFIRMACAO.equalsIgnoreCase(normalizar(corpo));
	}

	// Tira só pontuação nas pontas (ex: "confirmar!", "confirmar."): continua exigindo que a palavra em si seja
	// exatamente "confirmar", não vira um match por substring que poderia confundir com uma negação.
	private static String normalizar(String texto) {
		return texto == null ? "" : texto.trim().replaceAll("^[!?.,;:]+|[!?.,;:]+$", "");
	}
}
