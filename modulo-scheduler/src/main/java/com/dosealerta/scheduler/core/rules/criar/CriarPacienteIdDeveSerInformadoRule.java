package com.dosealerta.scheduler.core.rules.criar;

import com.dosealerta.scheduler.core.exception.PacienteIdObrigatorioException;

public class CriarPacienteIdDeveSerInformadoRule implements ValidadorCriacaoAlarmeRule {

	@Override
	public void validar(CriacaoAlarmeContext context) {
		if (context.input().pacienteId() == null) {
			throw new PacienteIdObrigatorioException();
		}
	}
}
