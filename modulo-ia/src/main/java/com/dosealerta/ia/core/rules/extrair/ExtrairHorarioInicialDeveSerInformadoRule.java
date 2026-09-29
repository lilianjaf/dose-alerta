package com.dosealerta.ia.core.rules.extrair;

import com.dosealerta.ia.core.exception.HorarioInicialObrigatorioException;

public class ExtrairHorarioInicialDeveSerInformadoRule implements ValidadorExtracaoReceitaRule {

	@Override
	public void validar(ExtracaoReceitaContext context) {
		if (context.input().horarioInicial() == null) {
			throw new HorarioInicialObrigatorioException();
		}
	}
}
