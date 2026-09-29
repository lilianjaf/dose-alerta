package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import com.dosealerta.relatorioadesao.core.exception.PacienteIdObrigatorioException;

public class RegistrarInteracaoPacienteIdDeveSerInformadoRule implements ValidadorRegistroInteracaoRule {

	@Override
	public void validar(RegistroInteracaoContext context) {
		if (context.input().pacienteId() == null) {
			throw new PacienteIdObrigatorioException();
		}
	}
}
