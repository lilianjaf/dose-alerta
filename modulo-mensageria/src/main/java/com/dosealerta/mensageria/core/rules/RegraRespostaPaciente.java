package com.dosealerta.mensageria.core.rules;

import java.util.Set;

public final class RegraRespostaPaciente {

	// "confirmar" é sobre os DADOS da receita extraída; "tomei" é sobre a DOSE do alarme. Palavras separadas de
	// propósito: se as duas caíssem no mesmo grupo, "TOMEI" enviado com uma receita ainda pendente na fila
	// confirmaria essa receita em vez de responder à pergunta sobre a dose.
	private static final Set<String> PALAVRAS_DE_CONFIRMACAO_DE_RECEITA =
			Set.of("confirmar", "confirmo", "confirma", "confirmado");

	private static final Set<String> PALAVRAS_DE_CONFIRMACAO_DE_DOSE = Set.of("tomei", "tomo", "tomado");

	private static final Set<String> PALAVRAS_DE_NEGACAO = Set.of("não tomei", "nao tomei", "não", "nao");

	private RegraRespostaPaciente() {
	}

	// Usado só no fluxo de rotina do alarme (fora do contexto de receita), onde não há essa ambiguidade: tanto
	// "CONFIRMAR" quanto "TOMEI" confirmam a dose.
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
