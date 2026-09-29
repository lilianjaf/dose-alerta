package com.dosealerta.usuario.core.rules.cadastro;

import com.dosealerta.usuario.core.exception.SenhaObrigatoriaException;

public class CadastroSenhaDevePreenchidaRule implements ValidadorCadastroPacienteRule {

	@Override
	public void validar(CadastroPacienteContext context) {
		if (context.input().senha() == null || context.input().senha().isBlank()) {
			throw new SenhaObrigatoriaException();
		}
	}
}
