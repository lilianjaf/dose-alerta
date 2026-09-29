package com.dosealerta.usuario.core.rules.autenticacao;

import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;

public class AutenticacaoTelefoneDevePreenchidoRule implements ValidadorAutenticacaoRule {

	@Override
	public void validar(AutenticacaoContext context) {
		if (context.input().telefone() == null || context.input().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
