package com.dosealerta.mensageria.core.rules;

import com.dosealerta.mensageria.core.dto.ReceitaCriada;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RegraMensagemReceita {

	private static final String CAMPO_DOSE = "dose";
	private static final String CAMPO_FREQUENCIA = "frequenciaHoras";
	private static final String CAMPO_DURACAO = "duracaoDias";
	private static final Map<String, String> NOME_CAMPO = Map.of(
			CAMPO_DOSE, "dose",
			CAMPO_FREQUENCIA, "frequência",
			CAMPO_DURACAO, "duração");

	private static final String FORMATO_CORRECAO =
			"dose; frequência; duração (ex: 1 comprimido; 8 em 8 horas; 7 dias)";
	private static final String PEDIR_NUMERO_INSCRICAO_SUS =
			"Não encontramos seu cadastro no SUS pelo telefone. Para continuar, envie o número da sua "
					+ "inscrição no SUS (Cartão SUS).";
	private static final String NUMERO_INSCRICAO_SUS_NAO_ENCONTRADO =
			"Não encontramos esse número de inscrição do SUS. Confira e envie novamente o número do seu "
					+ "Cartão SUS.";
	private static final String BOAS_VINDAS =
			"Sou a SusIA, do Dose Alerta. Eu ajudo você a ler receitas e te ligo para você não esquecer de "
					+ "tomar seus medicamentos. Quando quiser, me envie uma foto da receita para eu criar os "
					+ "lembretes.";
	private static final String AJUDA =
			"Envie uma foto da sua receita para eu extrair os dados e criar os lembretes de medicação.";
	private static final String RECEITA_RECEBIDA = "Recebi a receita! Identifiquei:\n";
	private static final String ITEM_DA_LISTA = "- ";
	private static final String RESPONDER_CONFIRMAR =
			"\nResponda *CONFIRMAR* se estiver tudo certo, ou me diga o que corrigir no formato: ";
	private static final String AINDA_FALTAM_DADOS =
			"Ainda faltam alguns dados para eu confirmar. Me envie os que faltaram, um de cada vez:\n";
	private static final String PEDIR_DADOS_FALTANTES =
			"Para os demais, me envie os dados que faltaram, um de cada vez:\n";
	private static final String NAO_CONSEGUI_LER = "Não consegui ler: ";
	private static final String DESCRICAO_MEDICAMENTO = "%s: %s, de %s em %s horas, por %s dias";
	private static final String DESCRICAO_FREQUENCIA = "de %s em %s horas";
	private static final String DESCRICAO_DURACAO = "por %s dias";
	private static final String PREFIXO_FALTOU = " — faltou: ";
	private static final String NAO_CONSEGUI_IDENTIFICAR = "Não consegui identificar: ";
	private static final String RESPONDA_NO_FORMATO = ". Responda no formato: ";
	private static final String CONFIRMADA =
			"Perfeito! %s confirmado. Vou te avisar na hora de cada dose.\n\n"
					+ "Já que a primeira dose é agora: você já tomou? Responda *TOMEI* ou *NÃO TOMEI*.";
	private static final String PRIMEIRA_DOSE_REGISTRADA = "Ótimo, já registrei! Vou te avisar na próxima dose.";
	private static final String PRIMEIRA_DOSE_AINDA_NAO_TOMADA = "Sem problema, vou te lembrar na hora certa.";
	private static final String CORRECAO_NAO_ENTENDIDA =
			"Não entendi sua resposta. Se algo estiver errado, me diga no formato: ";
	private static final String SE_ESTIVER_TUDO_CERTO = "Se estiver tudo certo, responda *CONFIRMAR*.";
	private static final String SEPARADOR = ", ";
	private static final String PONTO_FINAL = ".";
	private static final String PONTO_QUEBRA_DUPLA = ".\n\n";
	private static final String PONTO_ESPACO = ". ";
	private static final String ABRE_PARENTESES = " (";
	private static final String FECHA_PARENTESES = ")";
	private static final String VAZIO = "";

	private RegraMensagemReceita() {
	}

	public static String pedirNumeroInscricaoSus() {
		return PEDIR_NUMERO_INSCRICAO_SUS;
	}

	public static String numeroInscricaoSusNaoEncontrado() {
		return NUMERO_INSCRICAO_SUS_NAO_ENCONTRADO;
	}

	public static String boasVindas() {
		return BOAS_VINDAS;
	}

	public static String ajuda() {
		return AJUDA;
	}

	public static String resumoExtracao(List<ReceitaCriada> receitas, List<String> naoProcessados) {
		List<ReceitaCriada> completas = receitas.stream().filter(ReceitaCriada::completa).toList();
		List<ReceitaCriada> pendentes = receitas.stream().filter(r -> !r.completa()).toList();

		StringBuilder texto = new StringBuilder();
		if (!completas.isEmpty()) {
			texto.append(RECEITA_RECEBIDA);
			completas.forEach(r -> texto.append(ITEM_DA_LISTA).append(descreverMedicamento(r)).append('\n'));
			texto.append(RESPONDER_CONFIRMAR).append(FORMATO_CORRECAO).append(PONTO_QUEBRA_DUPLA);
		}
		if (!pendentes.isEmpty()) {
			texto.append(completas.isEmpty() ? AINDA_FALTAM_DADOS : PEDIR_DADOS_FALTANTES);
			pendentes.forEach(r -> texto.append(ITEM_DA_LISTA).append(descreverParcial(r)).append('\n'));
			texto.append('\n');
		}
		if (!naoProcessados.isEmpty()) {
			texto.append(NAO_CONSEGUI_LER).append(String.join(SEPARADOR, naoProcessados)).append(PONTO_FINAL);
		}
		return texto.toString().strip();
	}

	private static String descreverMedicamento(ReceitaCriada receita) {
		return DESCRICAO_MEDICAMENTO.formatted(
				receita.medicamento(),
				receita.dose(),
				receita.frequenciaHoras(),
				receita.frequenciaHoras(),
				receita.duracaoDias());
	}

	private static String descreverParcial(ReceitaCriada receita) {
		List<String> conhecido = new ArrayList<>();
		if (receita.dose() != null) {
			conhecido.add(receita.dose());
		}
		if (receita.frequenciaHoras() != null) {
			conhecido.add(DESCRICAO_FREQUENCIA.formatted(receita.frequenciaHoras(), receita.frequenciaHoras()));
		}
		if (receita.duracaoDias() != null) {
			conhecido.add(DESCRICAO_DURACAO.formatted(receita.duracaoDias()));
		}
		String faltando = nomesDosCampos(receita.camposPendentes());
		return receita.medicamento()
				+ (conhecido.isEmpty() ? VAZIO : ABRE_PARENTESES + String.join(SEPARADOR, conhecido) + FECHA_PARENTESES)
				+ PREFIXO_FALTOU + faltando;
	}

	public static String pedirCamposPendentes(List<String> camposPendentes) {
		return NAO_CONSEGUI_IDENTIFICAR + nomesDosCampos(camposPendentes) + RESPONDA_NO_FORMATO + FORMATO_CORRECAO
				+ PONTO_FINAL;
	}

	private static String nomesDosCampos(List<String> campos) {
		return campos.stream()
				.map(campo -> NOME_CAMPO.getOrDefault(campo, campo))
				.reduce((a, b) -> a + SEPARADOR + b)
				.orElse(VAZIO);
	}

	public static String confirmada(String medicamento) {
		return CONFIRMADA.formatted(medicamento);
	}

	public static String primeiraDoseRegistrada() {
		return PRIMEIRA_DOSE_REGISTRADA;
	}

	public static String primeiraDoseAindaNaoTomada() {
		return PRIMEIRA_DOSE_AINDA_NAO_TOMADA;
	}

	public static String correcaoNaoEntendida() {
		return CORRECAO_NAO_ENTENDIDA + FORMATO_CORRECAO + PONTO_ESPACO + SE_ESTIVER_TUDO_CERTO;
	}
}
