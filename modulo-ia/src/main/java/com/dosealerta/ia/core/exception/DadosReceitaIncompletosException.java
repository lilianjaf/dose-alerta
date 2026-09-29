package com.dosealerta.ia.core.exception;

import java.util.List;

public class DadosReceitaIncompletosException extends RuntimeException {

	private static final String MENSAGEM_PREFIXO = "A receita não informa dados de ";
	private static final String SEPARADOR_MEDICAMENTO_CAMPOS = ": ";
	private static final String SEPARADOR_CAMPOS = ", ";
	private static final String MENSAGEM_ORIENTACAO = ". Pergunte ao paciente e envie esses campos na confirmação";

	private final List<String> camposPendentes;

	public DadosReceitaIncompletosException(String medicamento, List<String> camposPendentes) {
		super(MENSAGEM_PREFIXO + medicamento + SEPARADOR_MEDICAMENTO_CAMPOS + String.join(SEPARADOR_CAMPOS, camposPendentes)
				+ MENSAGEM_ORIENTACAO);
		this.camposPendentes = List.copyOf(camposPendentes);
	}

	public List<String> getCamposPendentes() {
		return camposPendentes;
	}
}
