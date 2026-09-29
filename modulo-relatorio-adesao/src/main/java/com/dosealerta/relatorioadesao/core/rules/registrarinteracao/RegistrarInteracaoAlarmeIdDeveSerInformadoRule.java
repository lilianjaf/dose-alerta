package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import com.dosealerta.relatorioadesao.core.exception.AlarmeIdObrigatorioException;

public class RegistrarInteracaoAlarmeIdDeveSerInformadoRule implements ValidadorRegistroInteracaoRule {

	@Override
	public void validar(RegistroInteracaoContext context) {
		if (context.input().alarmeId() == null) {
			throw new AlarmeIdObrigatorioException();
		}
	}
}
