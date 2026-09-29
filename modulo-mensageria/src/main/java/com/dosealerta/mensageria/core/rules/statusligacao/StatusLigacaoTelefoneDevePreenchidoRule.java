package com.dosealerta.mensageria.core.rules.statusligacao;

import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;

public class StatusLigacaoTelefoneDevePreenchidoRule implements ValidadorStatusLigacaoRule {

	@Override
	public void validar(StatusLigacaoContext context) {
		if (context.telefone() == null || context.telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
