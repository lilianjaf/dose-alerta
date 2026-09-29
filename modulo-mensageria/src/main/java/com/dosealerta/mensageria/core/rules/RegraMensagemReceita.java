package com.dosealerta.mensageria.core.rules;

import com.dosealerta.mensageria.core.dto.ReceitaCriada;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RegraMensagemReceita {

	private static final Map<String, String> NOME_CAMPO = Map.of(
			"dose", "dose",
			"frequenciaHoras", "frequência",
			"duracaoDias", "duração");

	private static final String FORMATO_CORRECAO =
			"dose; frequência; duração (ex: 1 comprimido; 8 em 8 horas; 7 dias)";

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
		return "Sou a SusIA, do Dose Alerta. Eu ajudo você a ler receitas e te ligo para você não esquecer de "
				+ "tomar seus medicamentos. Quando quiser, me envie uma foto da receita para eu criar os "
				+ "lembretes.";
	}

	public static String ajuda() {
		return "Envie uma foto da sua receita para eu extrair os dados e criar os lembretes de medicação.";
	}

	public static String resumoExtracao(List<ReceitaCriada> receitas, List<String> naoProcessados) {
		List<ReceitaCriada> completas = receitas.stream().filter(ReceitaCriada::completa).toList();
		List<ReceitaCriada> pendentes = receitas.stream().filter(r -> !r.completa()).toList();

		StringBuilder texto = new StringBuilder();
		if (!completas.isEmpty()) {
			texto.append("Recebi a receita! Identifiquei:\n");
			completas.forEach(r -> texto.append("- ").append(descreverMedicamento(r)).append('\n'));
			texto.append("\nResponda *CONFIRMAR* se estiver tudo certo, ou me diga o que corrigir no formato: ")
					.append(FORMATO_CORRECAO)
					.append(".\n\n");
		}
		if (!pendentes.isEmpty()) {
			texto.append(completas.isEmpty()
					? "Ainda faltam alguns dados para eu confirmar. Me envie os que faltaram, um de cada vez:\n"
					: "Para os demais, me envie os dados que faltaram, um de cada vez:\n");
			pendentes.forEach(r -> texto.append("- ").append(descreverParcial(r)).append('\n'));
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

	private static String descreverParcial(ReceitaCriada receita) {
		List<String> conhecido = new ArrayList<>();
		if (receita.dose() != null) {
			conhecido.add(receita.dose());
		}
		if (receita.frequenciaHoras() != null) {
			conhecido.add("de " + receita.frequenciaHoras() + " em " + receita.frequenciaHoras() + " horas");
		}
		if (receita.duracaoDias() != null) {
			conhecido.add("por " + receita.duracaoDias() + " dias");
		}
		String faltando = receita.camposPendentes().stream()
				.map(campo -> NOME_CAMPO.getOrDefault(campo, campo))
				.reduce((a, b) -> a + ", " + b)
				.orElse("");
		return receita.medicamento() + (conhecido.isEmpty() ? "" : " (" + String.join(", ", conhecido) + ")")
				+ " — faltou: " + faltando;
	}

	public static String pedirCamposPendentes(List<String> camposPendentes) {
		String nomesDosCampos = camposPendentes.stream()
				.map(campo -> NOME_CAMPO.getOrDefault(campo, campo))
				.reduce((a, b) -> a + ", " + b)
				.orElse("");
		return "Não consegui identificar: " + nomesDosCampos + ". Responda no formato: " + FORMATO_CORRECAO + ".";
	}

	// A primeira dose é considerada "agora" (ver horarioInicial na extração), então perguntamos direto em vez
	// de mandar o lembrete automático de algo que talvez o paciente já tenha tomado.
	public static String confirmada(String medicamento) {
		return "Perfeito! " + medicamento + " confirmado. Vou te avisar na hora de cada dose.\n\n"
				+ "Já que a primeira dose é agora: você já tomou? Responda *TOMEI* ou *NÃO TOMEI*.";
	}

	public static String primeiraDoseRegistrada() {
		return "Ótimo, já registrei! Vou te avisar na próxima dose.";
	}

	public static String primeiraDoseAindaNaoTomada() {
		return "Sem problema, vou te lembrar na hora certa.";
	}

	public static String correcaoNaoEntendida() {
		return "Não entendi sua resposta. Se algo estiver errado, me diga no formato: " + FORMATO_CORRECAO + ". "
				+ "Se estiver tudo certo, responda *CONFIRMAR*.";
	}
}
