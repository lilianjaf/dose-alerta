package com.dosealerta.usuario.core.rules.autenticacao;

import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;

public class AutenticacaoPacienteDeveExistirRule implements ValidadorAutenticacaoRule {

	@Override
	public void validar(AutenticacaoContext context) {
		if (context.paciente() == null) {
			throw new CredenciaisInvalidasException();
		}
	}
}
