package com.dosealerta.ia.core.rules;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import java.util.regex.Pattern;

public final class RegraValidacaoReceitaExtraida {

	private static final String MOTIVO_MEDICAMENTO_NAO_IDENTIFICADO = "medicamento não identificado";
	private static final Pattern PADRAO_DOSE = Pattern.compile(
			"^(?:\\d{1,4}(?:[.,]\\d{1,2})?|\\d{1,2}/\\d{1,2}|meio|meia)\\s?"
					+ "(?:mg|mcg|µg|g|ml|ui|comprimidos?|comp|cpr?s?|c[aá]psulas?|caps?|gotas?|gts?"
					+ "|doses?|jatos?|puffs?|inala[cç](?:[aã]o|[oõ]es)|aplica[cç](?:[aã]o|[oõ]es)|ampolas?|amp|sach[eê]s?|unidades?)\\.?$",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);


	private static final int FREQUENCIA_MIN_HORAS = 1;

	private static final int FREQUENCIA_MAX_HORAS = 168;
	private static final int DURACAO_MIN_DIAS = 1;
	private static final int DURACAO_MAX_DIAS = 365;

	private RegraValidacaoReceitaExtraida() {
	}

	public static MedicamentoExtraido normalizarMedicamento(MedicamentoExtraido medicamento) {
		if (medicamento.medicamento() == null || medicamento.medicamento().isBlank()) {
			throw new ReceitaInvalidaException(MOTIVO_MEDICAMENTO_NAO_IDENTIFICADO);
		}
		return new MedicamentoExtraido(
				medicamento.medicamento(),
				dosePlausivel(medicamento.dose()) ? medicamento.dose().trim() : null,
				dentroDaFaixa(medicamento.frequenciaHoras(), FREQUENCIA_MIN_HORAS, FREQUENCIA_MAX_HORAS)
						? medicamento.frequenciaHoras()
						: null,
				dentroDaFaixa(medicamento.duracaoDias(), DURACAO_MIN_DIAS, DURACAO_MAX_DIAS)
						? medicamento.duracaoDias()
						: null);
	}

	private static boolean dosePlausivel(String dose) {
		return dose != null && PADRAO_DOSE.matcher(dose.trim()).matches();
	}

	private static boolean dentroDaFaixa(Integer valor, int minimo, int maximo) {
		return valor != null && valor >= minimo && valor <= maximo;
	}
}
