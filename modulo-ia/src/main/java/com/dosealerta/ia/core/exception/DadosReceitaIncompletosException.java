package com.dosealerta.ia.core.exception;

import java.util.List;

public class DadosReceitaIncompletosException extends RuntimeException {

	private final List<String> camposPendentes;

	public DadosReceitaIncompletosException(String medicamento, List<String> camposPendentes) {
		super("A receita não informa dados de " + medicamento + ": " + String.join(", ", camposPendentes)
				+ ". Pergunte ao paciente e envie esses campos na confirmação");
		this.camposPendentes = List.copyOf(camposPendentes);
	}

	public List<String> getCamposPendentes() {
		return camposPendentes;
	}
}
