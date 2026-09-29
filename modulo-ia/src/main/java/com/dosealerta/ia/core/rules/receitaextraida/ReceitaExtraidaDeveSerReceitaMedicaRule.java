package com.dosealerta.ia.core.rules.receitaextraida;

import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;

public class ReceitaExtraidaDeveSerReceitaMedicaRule implements ValidadorReceitaExtraidaRule {

	private static final String MOTIVO = "a imagem não é uma receita";

	@Override
	public void validar(ReceitaExtraidaContext context) {
		if (!context.extraida().receitaMedica()) {
			throw new ReceitaFormalNaoIdentificadaException(MOTIVO);
		}
	}
}
