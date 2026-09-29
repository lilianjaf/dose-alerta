package com.dosealerta.notificacao.core.rules;

import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

public final class RegraConteudoNotificacao {

	// "TOMEI" pra ficar consistente com a pergunta feita logo após confirmar a receita (ver
	// RegraMensagemReceita.confirmada, no modulo-mensageria) — o mesmo mecanismo de confirmação de dose já
	// aceita as duas palavras, então a mensagem também deveria falar a mesma língua.
	public static final String TEXTO_BOTAO_CONFIRMACAO = "TOMEI";

	private RegraConteudoNotificacao() {
	}

	public static String textoMensagem(EtapaEscalonamento etapa, String medicamento, String dose) {
		return switch (etapa) {
			case LEMBRETE_INICIAL -> "Hora de tomar %s (%s).".formatted(medicamento, dose);
			case REFORCO -> "Lembrete: você ainda não confirmou %s (%s).".formatted(medicamento, dose);
			case LIGACAO -> throw new IllegalArgumentException("Etapa LIGACAO não usa mensagem de texto");
		};
	}

	public static String textoFalado(String medicamento, String dose) {
		return "Olá, aqui é o DoseAlerta. Está na hora de tomar %s, %s. Aperte 1 para confirmar."
				.formatted(medicamento, dose);
	}
}
