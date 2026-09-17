package com.dosealerta.mensageria.core.rules;

/**
 * Decide se uma resposta de texto/botão do WhatsApp equivale à confirmação da dose. O botão
 * de confirmação é sempre enviado com o texto {@link #TEXTO_CONFIRMACAO} (ver
 * RegraConteudoNotificacao no modulo-notificacao), mas o paciente também pode digitar a
 * mesma palavra manualmente em vez de tocar no botão.
 */
public final class RegraRespostaPaciente {

	public static final String TEXTO_CONFIRMACAO = "CONFIRMAR";

	private RegraRespostaPaciente() {
	}

	public static boolean ehConfirmacao(String corpo, String textoBotao) {
		return TEXTO_CONFIRMACAO.equalsIgnoreCase(normalizar(textoBotao))
				|| TEXTO_CONFIRMACAO.equalsIgnoreCase(normalizar(corpo));
	}

	private static String normalizar(String texto) {
		return texto == null ? "" : texto.trim();
	}
}
