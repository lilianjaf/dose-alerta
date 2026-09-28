package com.dosealerta.mensageria.core.rules;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import java.util.Optional;

/**
 * Interpreta a resposta do paciente ao pedido de dados faltantes: uma mensagem só, campos separados por
 * ponto-e-vírgula, na ordem dose; frequência em horas; duração em dias (ex: "1 comprimido; 8; 7").
 */
public final class RegraParseCorrecaoReceita {

	private static final int NUMERO_DE_CAMPOS = 3;

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
		try {
			return Integer.parseInt(texto.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
