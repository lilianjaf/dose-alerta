package com.dosealerta.relatorioadesao.core.rules.registrarinteracao;

import com.dosealerta.relatorioadesao.core.exception.MedicamentoObrigatorioException;

public class RegistrarInteracaoMedicamentoDevePreenchidoRule implements ValidadorRegistroInteracaoRule {

	@Override
	public void validar(RegistroInteracaoContext context) {
		if (context.input().medicamento() == null || context.input().medicamento().isBlank()) {
			throw new MedicamentoObrigatorioException();
		}
	}
}
