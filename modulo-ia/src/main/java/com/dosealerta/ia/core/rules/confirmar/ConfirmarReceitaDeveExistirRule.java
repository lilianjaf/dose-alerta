package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;

public class ConfirmarReceitaDeveExistirRule implements ValidadorConfirmacaoReceitaRule {

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		if (context.receita() == null) {
			throw new ReceitaNaoEncontradaException(context.receitaId());
		}
	}
}
