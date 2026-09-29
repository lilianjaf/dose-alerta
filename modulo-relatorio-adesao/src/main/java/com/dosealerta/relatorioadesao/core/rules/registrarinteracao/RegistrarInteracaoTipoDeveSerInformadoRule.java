package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import com.dosealerta.relatorioadesao.core.exception.TipoInteracaoObrigatorioException;

public class RegistrarInteracaoTipoDeveSerInformadoRule implements ValidadorRegistroInteracaoRule {

	@Override
	public void validar(RegistroInteracaoContext context) {
		if (context.input().tipo() == null) {
			throw new TipoInteracaoObrigatorioException();
		}
	}
}
