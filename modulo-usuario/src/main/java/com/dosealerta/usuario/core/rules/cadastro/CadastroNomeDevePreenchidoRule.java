package com.dosealerta.usuario.core.rules.cadastro;

import com.dosealerta.usuario.core.exception.NomeObrigatorioException;

public class CadastroNomeDevePreenchidoRule implements ValidadorCadastroPacienteRule {

	@Override
	public void validar(CadastroPacienteContext context) {
		if (context.input().nome() == null || context.input().nome().isBlank()) {
			throw new NomeObrigatorioException();
		}
	}
}
