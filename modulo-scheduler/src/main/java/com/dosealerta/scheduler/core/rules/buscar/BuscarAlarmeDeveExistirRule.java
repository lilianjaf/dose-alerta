package com.dosealerta.scheduler.core.rules.buscar;

import com.dosealerta.scheduler.core.exception.AlarmeNaoEncontradoException;

public class BuscarAlarmeDeveExistirRule implements ValidadorBuscaAlarmeRule {

	@Override
	public void validar(BuscaAlarmeContext context) {
		if (context.alarme() == null) {
			throw new AlarmeNaoEncontradoException(context.id());
		}
	}
}
