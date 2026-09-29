package com.dosealerta.scheduler.core.rules.buscar;

import com.dosealerta.scheduler.core.exception.AlarmeIdObrigatorioException;

public class BuscarAlarmeIdDeveSerInformadoRule implements ValidadorBuscaAlarmeRule {

	@Override
	public void validar(BuscaAlarmeContext context) {
		if (context.id() == null) {
			throw new AlarmeIdObrigatorioException();
		}
	}
}
