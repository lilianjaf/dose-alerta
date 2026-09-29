package com.dosealerta.ia.core.rules.confirmarportelefone;

import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;

public class ConfirmarPorTelefoneReceitaPendenteDeveExistirRule implements ValidadorConfirmacaoPorTelefoneRule {

	@Override
	public void validar(ConfirmacaoPorTelefoneContext context) {
		if (context.pendente() == null) {
			throw new ReceitaNaoEncontradaException(context.telefone());
		}
	}
}
