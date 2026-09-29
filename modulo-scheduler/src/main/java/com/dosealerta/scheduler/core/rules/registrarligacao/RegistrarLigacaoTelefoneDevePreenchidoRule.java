package com.dosealerta.scheduler.core.rules.registrarligacao;

import com.dosealerta.scheduler.core.exception.TelefoneObrigatorioException;

public class RegistrarLigacaoTelefoneDevePreenchidoRule implements ValidadorRegistroLigacaoRule {

	@Override
	public void validar(RegistroLigacaoContext context) {
		if (context.telefone() == null || context.telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
