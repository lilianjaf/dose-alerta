package com.dosealerta.mensageria.core.rules;

public final class RegraStatusLigacao {

	private static final String STATUS_ATENDIDA = "in-progress";

	private RegraStatusLigacao() {
	}

	public static boolean foiAtendida(String callStatus) {
		return STATUS_ATENDIDA.equalsIgnoreCase(callStatus);
	}
}
