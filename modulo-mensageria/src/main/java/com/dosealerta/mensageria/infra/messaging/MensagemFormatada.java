package com.dosealerta.mensageria.infra.messaging;

public record MensagemFormatada(String corpo, String contentSid, String contentVariablesJson) {

	public static MensagemFormatada deTexto(String corpo) {
		return new MensagemFormatada(corpo, null, null);
	}

	public static MensagemFormatada deTemplate(String contentSid, String contentVariablesJson) {
		return new MensagemFormatada("", contentSid, contentVariablesJson);
	}

	public boolean isTemplate() {
		return contentSid != null;
	}
}
