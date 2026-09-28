package com.dosealerta.mensageria.core.exception;

import java.util.List;

public class DadosReceitaIncompletosException extends RuntimeException {

	private final List<String> camposPendentes;

	public DadosReceitaIncompletosException(List<String> camposPendentes) {
		super("Dados incompletos para confirmar a receita: " + camposPendentes);
		this.camposPendentes = List.copyOf(camposPendentes);
	}

	public List<String> getCamposPendentes() {
		return camposPendentes;
	}
}
