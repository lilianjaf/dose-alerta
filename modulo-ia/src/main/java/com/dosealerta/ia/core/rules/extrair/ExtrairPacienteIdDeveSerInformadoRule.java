package com.dosealerta.ia.core.rules.extrair;

import com.dosealerta.ia.core.exception.PacienteIdObrigatorioException;

public class ExtrairPacienteIdDeveSerInformadoRule implements ValidadorExtracaoReceitaRule {

	@Override
	public void validar(ExtracaoReceitaContext context) {
		if (context.input().pacienteId() == null) {
			throw new PacienteIdObrigatorioException();
		}
	}
}
