package com.dosealerta.usuario.core.rules.autenticacao;

import com.dosealerta.usuario.core.exception.SenhaObrigatoriaException;

public class AutenticacaoSenhaDevePreenchidaRule implements ValidadorAutenticacaoRule {

	@Override
	public void validar(AutenticacaoContext context) {
		if (context.input().senha() == null || context.input().senha().isBlank()) {
			throw new SenhaObrigatoriaException();
		}
	}
}
