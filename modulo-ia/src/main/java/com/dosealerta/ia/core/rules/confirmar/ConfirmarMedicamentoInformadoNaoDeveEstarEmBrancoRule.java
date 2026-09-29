package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.exception.CampoInformadoEmBrancoException;

public class ConfirmarMedicamentoInformadoNaoDeveEstarEmBrancoRule implements ValidadorConfirmacaoReceitaRule {

	private static final String CAMPO = "medicamento";

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		String valor = context.medicamento();
		if (valor != null && valor.isBlank()) {
			throw new CampoInformadoEmBrancoException(CAMPO);
		}
	}
}
