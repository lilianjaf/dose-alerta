package com.dosealerta.scheduler.core.rules.registrarconfirmacao;

import com.dosealerta.scheduler.core.exception.TelefoneObrigatorioException;

public class RegistrarConfirmacaoTelefoneDevePreenchidoRule implements ValidadorRegistroConfirmacaoRule {

	@Override
	public void validar(RegistroConfirmacaoContext context) {
		if (context.telefone() == null || context.telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
