package com.dosealerta.mensageria.core.rules.respostamensagem;

import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;

public class RespostaMensagemTelefoneDevePreenchidoRule implements ValidadorRespostaMensagemRule {

	@Override
	public void validar(RespostaMensagemContext context) {
		if (context.telefone() == null || context.telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
