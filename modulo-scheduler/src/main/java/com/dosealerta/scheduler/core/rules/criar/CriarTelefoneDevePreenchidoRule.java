package com.dosealerta.scheduler.core.rules.criar;

import com.dosealerta.scheduler.core.exception.TelefoneObrigatorioException;

public class CriarTelefoneDevePreenchidoRule implements ValidadorCriacaoAlarmeRule {

	@Override
	public void validar(CriacaoAlarmeContext context) {
		if (context.input().telefone() == null || context.input().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
