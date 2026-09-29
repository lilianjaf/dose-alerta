package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.exception.FrequenciaHorasForaDaFaixaException;

public class ConfirmarFrequenciaDeveEstarNaFaixaRule implements ValidadorConfirmacaoReceitaRule {

	private static final int MINIMO = 1;
	private static final int MAXIMO = 168;

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		Integer valor = context.frequenciaHoras();
		if (valor != null && (valor < MINIMO || valor > MAXIMO)) {
			throw new FrequenciaHorasForaDaFaixaException();
		}
	}
}
