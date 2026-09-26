package com.dosealerta.ia.core.rules;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import java.util.regex.Pattern;

public final class RegraValidacaoReceitaExtraida {

	// Quantidade (decimal com ponto ou vírgula, fração ou "meio/meia") + unidade, por extenso ou abreviada
	// como aparece nas receitas (cp, comp, cap, gts, amp...).
	private static final Pattern PADRAO_DOSE = Pattern.compile(
			"^(?:\\d{1,4}(?:[.,]\\d{1,2})?|\\d{1,2}/\\d{1,2}|meio|meia)\\s?"
					+ "(?:mg|mcg|µg|g|ml|ui|comprimidos?|comp|cpr?s?|c[aá]psulas?|caps?|gotas?|gts?"
					+ "|doses?|jatos?|puffs?|inala[cç](?:[aã]o|[oõ]es)|aplica[cç](?:[aã]o|[oõ]es)|ampolas?|amp|sach[eê]s?|unidades?)\\.?$",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	// Registro no conselho (CRM ou CRO) tem de 4 a 7 dígitos; a UF e a pontuação ("CRM-SP 70.760") não contam.
	private static final int REGISTRO_MIN_DIGITOS = 4;
	private static final int REGISTRO_MAX_DIGITOS = 7;

	private static final int FREQUENCIA_MIN_HORAS = 1;
	// 168h = semanal (ex: vitamina D 1x por semana); doses mais espaçadas que isso não são lembretes de rotina.
	private static final int FREQUENCIA_MAX_HORAS = 168;
	private static final int DURACAO_MIN_DIAS = 1;
	private static final int DURACAO_MAX_DIAS = 365;

	private RegraValidacaoReceitaExtraida() {
	}

	/** A imagem tem de ser uma receita formal, assinada por um profissional identificado (nome e registro). */
	public static void validarReceitaFormal(ReceitaExtraida extraida) {
		if (!extraida.receitaMedica()) {
			throw new ReceitaFormalNaoIdentificadaException("a imagem não é uma receita");
		}
		if (extraida.nomePrescritor() == null || extraida.nomePrescritor().isBlank()) {
			throw new ReceitaFormalNaoIdentificadaException("nome do profissional não identificado");
		}
		if (!registroPlausivel(extraida.registroProfissional())) {
			throw new ReceitaFormalNaoIdentificadaException("registro profissional (CRM/CRO) não identificado");
		}
		if (extraida.medicamentos() == null || extraida.medicamentos().isEmpty()) {
			throw new ReceitaInvalidaException("nenhum medicamento identificado");
		}
	}

	/**
	 * Só o nome do medicamento é indispensável para listá-lo ao paciente. Dose, frequência e duração ausentes ou
	 * implausíveis (fora do formato ou da faixa esperada) viram nulas, em vez de descartar o medicamento ou
	 * guardar um valor duvidoso: o paciente as informa na confirmação, que exige todas.
	 */
	public static MedicamentoExtraido normalizarMedicamento(MedicamentoExtraido medicamento) {
		if (medicamento.medicamento() == null || medicamento.medicamento().isBlank()) {
			throw new ReceitaInvalidaException("medicamento não identificado");
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

	private static boolean registroPlausivel(String registro) {
		if (registro == null) {
			return false;
		}
		long digitos = registro.chars().filter(Character::isDigit).count();
		return digitos >= REGISTRO_MIN_DIGITOS && digitos <= REGISTRO_MAX_DIGITOS;
	}
}
