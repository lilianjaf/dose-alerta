package com.dosealerta.ia.core.rules;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import java.util.regex.Pattern;

public final class RegraValidacaoReceitaExtraida {

	private static final Pattern PADRAO_DOSE = Pattern.compile(
			"^\\d{1,4}(\\.\\d{1,2})?\\s?(mg|mcg|g|ml|ui|comprimidos?|c[aá]psulas?|gotas?|doses?|jatos?|puffs?|inala[cç](?:[aã]o|[oõ]es))$",
			Pattern.CASE_INSENSITIVE);

	// CRM tem de 4 a 7 dígitos; a UF e a pontuação ("CRM-SP 70.760") não contam.
	private static final int CRM_MIN_DIGITOS = 4;
	private static final int CRM_MAX_DIGITOS = 7;

	private static final int FREQUENCIA_MIN_HORAS = 1;
	private static final int FREQUENCIA_MAX_HORAS = 24;
	private static final int DURACAO_MIN_DIAS = 1;
	private static final int DURACAO_MAX_DIAS = 365;

	private RegraValidacaoReceitaExtraida() {
	}

	public static void validar(ReceitaExtraida extraida) {
		validarReceitaFormal(extraida);
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

	private static void validarReceitaFormal(ReceitaExtraida extraida) {
		if (!extraida.receitaMedica()) {
			throw new ReceitaFormalNaoIdentificadaException("a imagem não é uma receita médica");
		}
		if (extraida.nomeMedico() == null || extraida.nomeMedico().isBlank()) {
			throw new ReceitaFormalNaoIdentificadaException("nome do médico não identificado");
		}
		if (!crmPlausivel(extraida.crm())) {
			throw new ReceitaFormalNaoIdentificadaException("CRM não identificado");
		}
	}

	private static boolean crmPlausivel(String crm) {
		if (crm == null) {
			return false;
		}
		long digitos = crm.chars().filter(Character::isDigit).count();
		return digitos >= CRM_MIN_DIGITOS && digitos <= CRM_MAX_DIGITOS;
	}
}
