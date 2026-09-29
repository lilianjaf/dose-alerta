package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import com.dosealerta.relatorioadesao.core.exception.InteracaoIdObrigatorioException;

public class RegistrarInteracaoIdDeveSerInformadoRule implements ValidadorRegistroInteracaoRule {

	@Override
	public void validar(RegistroInteracaoContext context) {
		if (context.input().id() == null) {
			throw new InteracaoIdObrigatorioException();
		}
	}
}
