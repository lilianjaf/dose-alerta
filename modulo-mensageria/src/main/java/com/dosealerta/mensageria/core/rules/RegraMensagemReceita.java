package com.dosealerta.mensageria.core.rules;

import java.util.List;
import java.util.Map;

/** Monta os textos enviados ao paciente durante a conversa de identificação e extração de receita. */
public final class RegraMensagemReceita {

	private static final Map<String, String> NOME_CAMPO = Map.of(
			"dose", "dose",
			"frequenciaHoras", "frequência",
			"duracaoDias", "duração");

	private RegraMensagemReceita() {
	}

	public static String pedirNumeroInscricaoSus() {
		return "Não encontramos seu cadastro no SUS pelo telefone. Para continuar, envie o número da sua "
				+ "inscrição no SUS (Cartão SUS).";
	}

	public static String numeroInscricaoSusNaoEncontrado() {
		return "Não encontramos esse número de inscrição do SUS. Confira e envie novamente o número do seu "
				+ "Cartão SUS.";
	}

	public static String boasVindas() {
		return "Cadastro confirmado! Quando quiser, me envie uma foto da receita para eu criar os lembretes.";
	}

	public static String ajuda() {
		return "Envie uma foto da sua receita para eu extrair os dados e criar os lembretes de medicação.";
	}

	public static String resumoExtracao(int receitasCompletas, int receitasPendentes, List<String> naoProcessados) {
		StringBuilder texto = new StringBuilder();
		if (receitasCompletas > 0) {
			texto.append("Recebi a receita! Responda *CONFIRMAR* para os medicamentos identificados corretamente.\n\n");
		}
		if (receitasPendentes > 0) {
			texto.append("Para os demais, me envie os dados que faltaram, um de cada vez.\n\n");
		}
		if (!naoProcessados.isEmpty()) {
			texto.append("Não consegui ler: ").append(String.join(", ", naoProcessados)).append(".");
		}
		return texto.toString().strip();
	}

	public static String pedirCamposPendentes(List<String> camposPendentes) {
		String nomesDosCampos = camposPendentes.stream()
				.map(campo -> NOME_CAMPO.getOrDefault(campo, campo))
				.reduce((a, b) -> a + ", " + b)
				.orElse("");
		return "Não consegui identificar: " + nomesDosCampos + ". Responda no formato: "
				+ "dose; frequência em horas; duração em dias (ex: 1 comprimido; 8; 7).";
	}

	public static String confirmada(String medicamento) {
		return "Perfeito! " + medicamento + " confirmado. Vou te avisar na hora de cada dose.";
	}
}
