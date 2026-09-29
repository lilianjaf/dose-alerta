package com.dosealerta.relatorioadesao.core.rules.consultartaxa;

import com.dosealerta.relatorioadesao.core.exception.PacienteIdObrigatorioException;

public class ConsultarTaxaPacienteIdDeveSerInformadoRule implements ValidadorConsultaTaxaAdesaoRule {

	@Override
	public void validar(ConsultaTaxaAdesaoContext context) {
		if (context.pacienteId() == null) {
			throw new PacienteIdObrigatorioException();
		}
	}
}
