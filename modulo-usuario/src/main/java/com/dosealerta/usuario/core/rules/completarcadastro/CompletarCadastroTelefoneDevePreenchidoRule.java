package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;

public class CompletarCadastroTelefoneDevePreenchidoRule implements ValidadorCompletarCadastroRule {

	@Override
	public void validar(CompletarCadastroContext context) {
		if (context.input().telefone() == null || context.input().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
