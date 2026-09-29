package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;

public class CompletarCadastroPacienteDeveExistirRule implements ValidadorCompletarCadastroRule {

	@Override
	public void validar(CompletarCadastroContext context) {
		if (context.paciente() == null) {
			throw new PacienteNaoEncontradoException(context.input().telefone());
		}
	}
}
