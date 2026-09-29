package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.exception.DadosReceitaIncompletosException;
import java.util.ArrayList;
import java.util.List;

public class ConfirmarDadosDaReceitaDevemEstarCompletosRule implements ValidadorConfirmacaoReceitaRule {

	private static final String CAMPO_DOSE = "dose";
	private static final String CAMPO_FREQUENCIA_HORAS = "frequenciaHoras";
	private static final String CAMPO_DURACAO_DIAS = "duracaoDias";

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		List<String> pendentes = new ArrayList<>();
		if (context.dose() == null || context.dose().isBlank()) {
			pendentes.add(CAMPO_DOSE);
		}
		if (context.frequenciaHoras() == null) {
			pendentes.add(CAMPO_FREQUENCIA_HORAS);
		}
		if (context.duracaoDias() == null) {
			pendentes.add(CAMPO_DURACAO_DIAS);
		}
		if (!pendentes.isEmpty()) {
			throw new DadosReceitaIncompletosException(context.medicamento(), pendentes);
		}
	}
}
