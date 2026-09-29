package com.dosealerta.mensageria.core.rules.mensagemrecebida;

import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;

public class MensagemRecebidaTelefoneDevePreenchidoRule implements ValidadorMensagemRecebidaRule {

	@Override
	public void validar(MensagemRecebidaContext context) {
		if (context.dados().telefone() == null || context.dados().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
