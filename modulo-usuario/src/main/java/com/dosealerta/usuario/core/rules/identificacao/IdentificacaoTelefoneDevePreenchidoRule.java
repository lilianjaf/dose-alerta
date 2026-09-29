package com.dosealerta.usuario.core.rules.identificacao;

import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;

public class IdentificacaoTelefoneDevePreenchidoRule implements ValidadorIdentificacaoPacienteRule {

	@Override
	public void validar(IdentificacaoPacienteContext context) {
		if (context.input().telefone() == null || context.input().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
