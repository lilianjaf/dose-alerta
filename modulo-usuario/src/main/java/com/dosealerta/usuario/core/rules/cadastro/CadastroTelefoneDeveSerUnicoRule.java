package com.dosealerta.usuario.core.rules.cadastro;

import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;

public class CadastroTelefoneDeveSerUnicoRule implements ValidadorCadastroPacienteRule {

	@Override
	public void validar(CadastroPacienteContext context) {
		if (context.telefoneJaCadastrado()) {
			throw new TelefoneJaCadastradoException(context.input().telefone());
		}
	}
}
