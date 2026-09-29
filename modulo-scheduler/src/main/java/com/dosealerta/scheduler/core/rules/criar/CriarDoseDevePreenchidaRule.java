package com.dosealerta.scheduler.core.rules.criar;

import com.dosealerta.scheduler.core.exception.DoseObrigatoriaException;

public class CriarDoseDevePreenchidaRule implements ValidadorCriacaoAlarmeRule {

	@Override
	public void validar(CriacaoAlarmeContext context) {
		if (context.input().dose() == null || context.input().dose().isBlank()) {
			throw new DoseObrigatoriaException();
		}
	}
}
