package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.exception.CampoInformadoEmBrancoException;

public class ConfirmarDoseInformadaNaoDeveEstarEmBrancoRule implements ValidadorConfirmacaoReceitaRule {

	private static final String CAMPO = "dose";

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		String valor = context.dose();
		if (valor != null && valor.isBlank()) {
			throw new CampoInformadoEmBrancoException(CAMPO);
		}
	}
}
