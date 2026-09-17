package com.dosealerta.mensageria.core.rules;

/**
 * Decide, a partir do {@code CallStatus} enviado pelo webhook de status do Twilio, se a
 * ligação foi de fato atendida pelo paciente. "in-progress" é o status emitido no instante
 * em que a chamada deixa de tocar e passa a estar em andamento — o sinal de atendimento.
 */
public final class RegraStatusLigacao {

	private static final String STATUS_ATENDIDA = "in-progress";

	private RegraStatusLigacao() {
	}

	public static boolean foiAtendida(String callStatus) {
		return STATUS_ATENDIDA.equalsIgnoreCase(callStatus);
	}
}
