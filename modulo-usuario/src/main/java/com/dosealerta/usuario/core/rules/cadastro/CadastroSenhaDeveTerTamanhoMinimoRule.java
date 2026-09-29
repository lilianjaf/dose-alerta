package com.dosealerta.usuario.core.rules.cadastro;

import com.dosealerta.usuario.core.exception.SenhaCurtaException;

public class CadastroSenhaDeveTerTamanhoMinimoRule implements ValidadorCadastroPacienteRule {

	private static final int TAMANHO_MINIMO = 8;

	@Override
	public void validar(CadastroPacienteContext context) {
		String senha = context.input().senha();
		if (senha != null && senha.length() < TAMANHO_MINIMO) {
			throw new SenhaCurtaException();
		}
	}
}
