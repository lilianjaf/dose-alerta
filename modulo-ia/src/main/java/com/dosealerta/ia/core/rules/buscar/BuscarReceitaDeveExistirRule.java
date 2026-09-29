package com.dosealerta.ia.core.rules.buscar;

import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;

public class BuscarReceitaDeveExistirRule implements ValidadorBuscaReceitaRule {

	@Override
	public void validar(BuscaReceitaContext context) {
		if (context.receita() == null) {
			throw new ReceitaNaoEncontradaException(context.id());
		}
	}
}
