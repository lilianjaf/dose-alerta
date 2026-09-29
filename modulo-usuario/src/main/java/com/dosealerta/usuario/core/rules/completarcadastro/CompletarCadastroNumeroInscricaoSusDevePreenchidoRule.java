package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.exception.NumeroInscricaoSusObrigatorioException;

public class CompletarCadastroNumeroInscricaoSusDevePreenchidoRule implements ValidadorCompletarCadastroRule {

	@Override
	public void validar(CompletarCadastroContext context) {
		if (context.input().numeroInscricaoSus() == null || context.input().numeroInscricaoSus().isBlank()) {
			throw new NumeroInscricaoSusObrigatorioException();
		}
	}
}
