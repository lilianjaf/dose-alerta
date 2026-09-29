package com.dosealerta.mensageria.core.rules;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegraParseCorrecaoReceita {

	private static final int NUMERO_DE_CAMPOS = 3;

	private static final Pattern PRIMEIRO_NUMERO = Pattern.compile("\\d+");

	private RegraParseCorrecaoReceita() {
	}

	public static Optional<CorrecaoReceita> parsear(String corpo) {
		if (corpo == null) {
			return Optional.empty();
		}
		String[] partes = corpo.split(";");
		if (partes.length != NUMERO_DE_CAMPOS) {
			return Optional.empty();
		}

		String dose = partes[0].trim();
		Integer frequenciaHoras = paraInteiro(partes[1]);
		Integer duracaoDias = paraInteiro(partes[2]);
		if (dose.isEmpty() || frequenciaHoras == null || duracaoDias == null) {
			return Optional.empty();
		}

		return Optional.of(new CorrecaoReceita(null, dose, frequenciaHoras, duracaoDias));
	}

	private static Integer paraInteiro(String texto) {
		Matcher matcher = PRIMEIRO_NUMERO.matcher(texto);
		if (!matcher.find()) {
			return null;
		}
		return Integer.parseInt(matcher.group());
	}
}
