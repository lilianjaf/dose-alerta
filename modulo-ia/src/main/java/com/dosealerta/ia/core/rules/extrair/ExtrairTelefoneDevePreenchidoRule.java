package com.dosealerta.ia.core.rules.extrair;

import com.dosealerta.ia.core.exception.TelefoneObrigatorioException;

public class ExtrairTelefoneDevePreenchidoRule implements ValidadorExtracaoReceitaRule {

	@Override
	public void validar(ExtracaoReceitaContext context) {
		if (context.input().telefone() == null || context.input().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
