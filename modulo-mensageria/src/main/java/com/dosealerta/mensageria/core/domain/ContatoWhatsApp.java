package com.dosealerta.mensageria.core.domain;

import java.util.regex.Pattern;

public record ContatoWhatsApp(String telefone) {

	private static final String MENSAGEM_TELEFONE_INVALIDO = "Telefone deve estar em formato E.164, ex: +5511999999999";
	private static final Pattern TELEFONE_E164 = Pattern.compile("^\\+[0-9]{10,15}$");

	public ContatoWhatsApp {
		if (telefone == null || !TELEFONE_E164.matcher(telefone).matches()) {
			throw new IllegalArgumentException(MENSAGEM_TELEFONE_INVALIDO);
		}
	}
}
