package com.dosealerta.usuario.core.rules.cadastro;

import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;

public class CadastroTelefoneDevePreenchidoRule implements ValidadorCadastroPacienteRule {

	@Override
	public void validar(CadastroPacienteContext context) {
		if (context.input().telefone() == null || context.input().telefone().isBlank()) {
			throw new TelefoneObrigatorioException();
		}
	}
}
