package com.dosealerta.ia.core.rules;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import java.util.regex.Pattern;

public final class RegraValidacaoReceitaExtraida {

	private static final Pattern PADRAO_DOSE = Pattern.compile(
			"^\\d{1,4}(\\.\\d{1,2})?\\s?(mg|mcg|g|ml|ui|comprimidos?|c[aá]psulas?|gotas?)$",
			Pattern.CASE_INSENSITIVE);

	private static final int FREQUENCIA_MIN_HORAS = 1;
	private static final int FREQUENCIA_MAX_HORAS = 24;
	private static final int DURACAO_MIN_DIAS = 1;
	private static final int DURACAO_MAX_DIAS = 365;

	private RegraValidacaoReceitaExtraida() {
	}

	public static void validar(ReceitaExtraida extraida) {
		if (extraida.medicamento() == null || extraida.medicamento().isBlank()) {
			throw new ReceitaInvalidaException("medicamento não identificado");
		}
		if (extraida.dose() == null || !PADRAO_DOSE.matcher(extraida.dose().trim()).matches()) {
			throw new ReceitaInvalidaException("dose fora do formato esperado: '" + extraida.dose() + "'");
		}
		if (extraida.frequenciaHoras() < FREQUENCIA_MIN_HORAS || extraida.frequenciaHoras() > FREQUENCIA_MAX_HORAS) {
			throw new ReceitaInvalidaException("frequência implausível: " + extraida.frequenciaHoras() + "h");
		}
		if (extraida.duracaoDias() < DURACAO_MIN_DIAS || extraida.duracaoDias() > DURACAO_MAX_DIAS) {
			throw new ReceitaInvalidaException("duração de tratamento implausível: " + extraida.duracaoDias() + " dias");
		}
	}
}
