package com.dosealerta.usuario.core.rules.autenticacao;

import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;

public class AutenticacaoSenhaDeveConferirRule implements ValidadorAutenticacaoRule {

	@Override
	public void validar(AutenticacaoContext context) {
		if (!context.senhaConfere()) {
			throw new CredenciaisInvalidasException();
		}
	}
}
