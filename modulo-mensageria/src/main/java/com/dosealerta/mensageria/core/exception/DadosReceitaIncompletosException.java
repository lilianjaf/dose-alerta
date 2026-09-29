package com.dosealerta.mensageria.core.exception;

import java.util.List;

public class DadosReceitaIncompletosException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "Dados incompletos para confirmar a receita: ";

	private final List<String> camposPendentes;

	public DadosReceitaIncompletosException(List<String> camposPendentes) {
		super(MENSAGEM_PREFIXO + camposPendentes);
		this.camposPendentes = List.copyOf(camposPendentes);
	}

	public List<String> getCamposPendentes() {
		return camposPendentes;
	}
}
