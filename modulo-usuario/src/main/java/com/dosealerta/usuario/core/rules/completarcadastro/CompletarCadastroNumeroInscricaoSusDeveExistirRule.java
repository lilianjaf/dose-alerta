package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;

public class CompletarCadastroNumeroInscricaoSusDeveExistirRule implements ValidadorCompletarCadastroRule {

	@Override
	public void validar(CompletarCadastroContext context) {
		if (context.nomeNoSus() == null) {
			throw new NumeroInscricaoSusNaoEncontradoException(context.input().numeroInscricaoSus());
		}
	}
}
