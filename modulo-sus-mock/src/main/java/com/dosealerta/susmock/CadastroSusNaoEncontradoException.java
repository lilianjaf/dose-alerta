package com.dosealerta.susmock;

class CadastroSusNaoEncontradoException extends RuntimeException {

	private static final String MENSAGEM = "Cadastro do SUS não encontrado: ";

	CadastroSusNaoEncontradoException(String identificador) {
		super(MENSAGEM + identificador);
	}
}
