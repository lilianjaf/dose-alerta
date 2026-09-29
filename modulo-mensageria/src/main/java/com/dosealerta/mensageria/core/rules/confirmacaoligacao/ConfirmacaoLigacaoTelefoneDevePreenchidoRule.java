package com.dosealerta.mensageria.core.rules.confirmacaoligacao;

import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;

public class ConfirmacaoLigacaoTelefoneDevePreenchidoRule implements ValidadorConfirmacaoLigacaoRule {

	@Override
	public void validar(ConfirmacaoLigacaoContext context) {
		if (context.telefone() == null || context.telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
