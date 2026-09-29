package com.dosealerta.mensageria.core.rules;

import java.util.Set;

public final class RegraRespostaPaciente {

	private static final Set<String> PALAVRAS_DE_CONFIRMACAO_DE_RECEITA =
			Set.of("confirmar", "confirmo", "confirma", "confirmado");

	private static final Set<String> PALAVRAS_DE_CONFIRMACAO_DE_DOSE = Set.of("tomei", "tomo", "tomado");

	private static final Set<String> PALAVRAS_DE_NEGACAO = Set.of("não tomei", "nao tomei", "não", "nao");

	private RegraRespostaPaciente() {
	}

	public static boolean ehConfirmacao(String corpo, String textoBotao) {
		return ehConfirmacaoDeReceita(corpo, textoBotao) || ehConfirmacaoDeDose(corpo, textoBotao);
	}

	public static boolean ehConfirmacaoDeReceita(String corpo, String textoBotao) {
		return contem(PALAVRAS_DE_CONFIRMACAO_DE_RECEITA, corpo, textoBotao);
	}

	public static boolean ehConfirmacaoDeDose(String corpo, String textoBotao) {
		return contem(PALAVRAS_DE_CONFIRMACAO_DE_DOSE, corpo, textoBotao);
	}

	public static boolean ehNegacao(String corpo, String textoBotao) {
		return contem(PALAVRAS_DE_NEGACAO, corpo, textoBotao);
	}

	private static boolean contem(Set<String> palavras, String corpo, String textoBotao) {
		return palavras.contains(normalizar(textoBotao)) || palavras.contains(normalizar(corpo));
	}

	private static String normalizar(String texto) {
		return texto == null ? "" : texto.trim().toLowerCase().replaceAll("^[!?.,;:]+|[!?.,;:]+$", "");
	}
}
