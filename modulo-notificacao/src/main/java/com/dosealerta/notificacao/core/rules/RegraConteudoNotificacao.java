package com.dosealerta.notificacao.core.rules;

import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

public final class RegraConteudoNotificacao {

	private static final String MENSAGEM_LEMBRETE_INICIAL = "Hora de tomar %s (%s).";
	private static final String MENSAGEM_REFORCO = "Lembrete: você ainda não confirmou %s (%s).";
	private static final String MENSAGEM_ETAPA_LIGACAO_SEM_TEXTO = "Etapa LIGACAO não usa mensagem de texto";
	private static final String TEXTO_FALADO_LIGACAO = "Olá, aqui é o DoseAlerta. Está na hora de tomar %s, %s. Aperte 1 para confirmar.";
	public static final String TEXTO_BOTAO_CONFIRMACAO = "TOMEI";

	private RegraConteudoNotificacao() {
	}

	public static String textoMensagem(EtapaEscalonamento etapa, String medicamento, String dose) {
		return switch (etapa) {
			case LEMBRETE_INICIAL -> MENSAGEM_LEMBRETE_INICIAL.formatted(medicamento, dose);
			case REFORCO -> MENSAGEM_REFORCO.formatted(medicamento, dose);
			case LIGACAO -> throw new IllegalArgumentException(MENSAGEM_ETAPA_LIGACAO_SEM_TEXTO);
		};
	}

	public static String textoFalado(String medicamento, String dose) {
		return TEXTO_FALADO_LIGACAO
				.formatted(medicamento, dose);
	}
}
