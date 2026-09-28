package com.dosealerta.mensageria.core.rules;

import com.dosealerta.mensageria.core.dto.ReceitaCriada;
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

	// Lista o que foi identificado (não só a contagem): o paciente precisa ver dose/frequência/duração antes de
	// responder CONFIRMAR, não confirmar às cegas.
	public static String resumoExtracao(List<ReceitaCriada> receitas, List<String> naoProcessados) {
		List<ReceitaCriada> completas = receitas.stream().filter(ReceitaCriada::completa).toList();
		List<ReceitaCriada> pendentes = receitas.stream().filter(r -> !r.completa()).toList();

		StringBuilder texto = new StringBuilder();
		if (!completas.isEmpty()) {
			texto.append("Recebi a receita! Identifiquei:\n");
			completas.forEach(r -> texto.append("- ").append(descreverMedicamento(r)).append('\n'));
			texto.append("\nResponda *CONFIRMAR* se estiver tudo certo, ou me diga o que corrigir no formato: ")
					.append("dose; frequência em horas; duração em dias (ex: 1 comprimido; 8; 7).\n\n");
		}
		if (!pendentes.isEmpty()) {
			texto.append("Para os demais, me envie os dados que faltaram, um de cada vez:\n");
			pendentes.forEach(r -> texto.append("- ").append(r.medicamento()).append('\n'));
			texto.append('\n');
		}
		if (!naoProcessados.isEmpty()) {
			texto.append("Não consegui ler: ").append(String.join(", ", naoProcessados)).append(".");
		}
		return texto.toString().strip();
	}

	private static String descreverMedicamento(ReceitaCriada receita) {
		return receita.medicamento() + ": " + receita.dose() + ", de " + receita.frequenciaHoras() + " em "
				+ receita.frequenciaHoras() + " horas, por " + receita.duracaoDias() + " dias";
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

	public static String correcaoNaoEntendida() {
		return "Não entendi sua resposta. Se algo estiver errado, me diga no formato: "
				+ "dose; frequência em horas; duração em dias (ex: 1 comprimido; 8; 7). "
				+ "Se estiver tudo certo, responda *CONFIRMAR*.";
	}
}
