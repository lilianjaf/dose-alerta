package com.dosealerta.relatorioadesao.core.rules.consultartaxa;

import com.dosealerta.relatorioadesao.core.exception.PeriodoInvalidoException;

public class ConsultarTaxaPeriodoDeveSerValidoRule implements ValidadorConsultaTaxaAdesaoRule {

	@Override
	public void validar(ConsultaTaxaAdesaoContext context) {
		if (context.inicio().isAfter(context.fim())) {
			throw new PeriodoInvalidoException(context.inicio(), context.fim());
		}
	}
}
