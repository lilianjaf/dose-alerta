package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import com.dosealerta.relatorioadesao.core.exception.RegistradaEmObrigatoriaException;

public class RegistrarInteracaoRegistradaEmDeveSerInformadaRule implements ValidadorRegistroInteracaoRule {

	@Override
	public void validar(RegistroInteracaoContext context) {
		if (context.input().registradaEm() == null) {
			throw new RegistradaEmObrigatoriaException();
		}
	}
}
