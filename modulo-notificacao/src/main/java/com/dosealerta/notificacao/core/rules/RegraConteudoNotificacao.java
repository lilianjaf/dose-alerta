package com.dosealerta.notificacao.core.rules;

import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

public final class RegraConteudoNotificacao {

	public static final String TEXTO_BOTAO_CONFIRMACAO = "CONFIRMAR";

	private RegraConteudoNotificacao() {
	}

	public static String textoMensagem(EtapaEscalonamento etapa, String medicamento, String dose) {
		return switch (etapa) {
			case LEMBRETE_INICIAL -> "Hora de tomar %s (%s). Responda *%s* para confirmar."
					.formatted(medicamento, dose, TEXTO_BOTAO_CONFIRMACAO);
			case REFORCO -> "Lembrete: você ainda não confirmou %s (%s). Responda *%s* para confirmar."
					.formatted(medicamento, dose, TEXTO_BOTAO_CONFIRMACAO);
			case LIGACAO -> throw new IllegalArgumentException("Etapa LIGACAO não usa mensagem de texto");
		};
	}

	public static String textoFalado(String medicamento, String dose) {
		return "Olá, aqui é o DoseAlerta. Está na hora de tomar %s, %s. Aperte 1 para confirmar."
				.formatted(medicamento, dose);
	}
}
