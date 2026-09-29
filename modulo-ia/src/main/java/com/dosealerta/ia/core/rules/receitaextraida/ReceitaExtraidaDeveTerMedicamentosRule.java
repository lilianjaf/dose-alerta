package com.dosealerta.ia.core.rules.receitaextraida;

import com.dosealerta.ia.core.exception.ReceitaInvalidaException;

public class ReceitaExtraidaDeveTerMedicamentosRule implements ValidadorReceitaExtraidaRule {

	private static final String MOTIVO = "nenhum medicamento identificado";

	@Override
	public void validar(ReceitaExtraidaContext context) {
		if (context.extraida().medicamentos() == null || context.extraida().medicamentos().isEmpty()) {
			throw new ReceitaInvalidaException(MOTIVO);
		}
	}
}
