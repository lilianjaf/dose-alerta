package com.dosealerta.ia.core.rules.buscar;

import com.dosealerta.ia.core.exception.ReceitaIdObrigatorioException;

public class BuscarReceitaIdDeveSerInformadoRule implements ValidadorBuscaReceitaRule {

	@Override
	public void validar(BuscaReceitaContext context) {
		if (context.id() == null) {
			throw new ReceitaIdObrigatorioException();
		}
	}
}
