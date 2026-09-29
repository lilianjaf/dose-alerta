package com.dosealerta.ia.core.rules.confirmarportelefone;

import com.dosealerta.ia.core.exception.TelefoneObrigatorioException;

public class ConfirmarPorTelefoneTelefoneDevePreenchidoRule implements ValidadorConfirmacaoPorTelefoneRule {

	@Override
	public void validar(ConfirmacaoPorTelefoneContext context) {
		if (context.telefone() == null || context.telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
