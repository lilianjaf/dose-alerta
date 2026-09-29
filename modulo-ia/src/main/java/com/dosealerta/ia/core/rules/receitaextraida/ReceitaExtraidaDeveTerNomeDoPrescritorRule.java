package com.dosealerta.ia.core.rules.receitaextraida;

import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;

public class ReceitaExtraidaDeveTerNomeDoPrescritorRule implements ValidadorReceitaExtraidaRule {

	private static final String MOTIVO = "nome do profissional não identificado";

	@Override
	public void validar(ReceitaExtraidaContext context) {
		String nome = context.extraida().nomePrescritor();
		if (nome == null || nome.isBlank()) {
			throw new ReceitaFormalNaoIdentificadaException(MOTIVO);
		}
	}
}
