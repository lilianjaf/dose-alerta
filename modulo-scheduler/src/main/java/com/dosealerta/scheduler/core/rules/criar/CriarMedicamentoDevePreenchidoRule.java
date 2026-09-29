package com.dosealerta.scheduler.core.rules.criar;

import com.dosealerta.scheduler.core.exception.MedicamentoObrigatorioException;

public class CriarMedicamentoDevePreenchidoRule implements ValidadorCriacaoAlarmeRule {

	@Override
	public void validar(CriacaoAlarmeContext context) {
		if (context.input().medicamento() == null || context.input().medicamento().isBlank()) {
			throw new MedicamentoObrigatorioException();
		}
	}
}
