package com.dosealerta.mensageria.infra.twilio;

import com.twilio.exception.ApiException;

public final class DetalheErroTwilio {

	private static final String ERRO_DESCONHECIDO = "erro desconhecido";
	private static final String TELEFONE_MASCARADO = "****";

	private DetalheErroTwilio() {
	}

	public static String descrever(Throwable erro) {
		for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
			if (atual instanceof ApiException api) {
				Integer http = api.getHttpStatusCode() != null ? api.getHttpStatusCode() : api.getStatusCode();
				return "Twilio HTTP " + http + ", código " + api.getCode() + ": " + api.getMessage()
						+ (api.getMoreInfo() != null ? " (" + api.getMoreInfo() + ")" : "");
			}
		}
		return erro == null ? ERRO_DESCONHECIDO : erro.getClass().getSimpleName() + ": " + erro.getMessage();
	}

	public static String mascarar(String telefone) {
		return telefone == null || telefone.length() <= 4 ? TELEFONE_MASCARADO : TELEFONE_MASCARADO + telefone.substring(telefone.length() - 4);
	}
}
