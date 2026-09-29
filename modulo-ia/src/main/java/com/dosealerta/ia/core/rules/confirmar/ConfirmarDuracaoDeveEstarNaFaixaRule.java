package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.exception.DuracaoDiasForaDaFaixaException;

public class ConfirmarDuracaoDeveEstarNaFaixaRule implements ValidadorConfirmacaoReceitaRule {

	private static final int MINIMO = 1;
	private static final int MAXIMO = 365;

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		Integer valor = context.duracaoDias();
		if (valor != null && (valor < MINIMO || valor > MAXIMO)) {
			throw new DuracaoDiasForaDaFaixaException();
		}
	}
}
