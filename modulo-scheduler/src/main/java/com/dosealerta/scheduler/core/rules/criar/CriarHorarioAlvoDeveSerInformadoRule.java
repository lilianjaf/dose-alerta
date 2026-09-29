package com.dosealerta.scheduler.core.rules.criar;

import com.dosealerta.scheduler.core.exception.HorarioAlvoObrigatorioException;

public class CriarHorarioAlvoDeveSerInformadoRule implements ValidadorCriacaoAlarmeRule {

	@Override
	public void validar(CriacaoAlarmeContext context) {
		if (context.input().horarioAlvo() == null) {
			throw new HorarioAlvoObrigatorioException();
		}
	}
}
