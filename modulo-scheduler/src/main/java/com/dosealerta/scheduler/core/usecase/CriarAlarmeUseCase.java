package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;

public class CriarAlarmeUseCase {

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;

	public CriarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
	}

	public Alarme executar(CriarAlarmeInput input) {
		Alarme alarme = Alarme.criar(input.pacienteId(), input.medicamento(), input.dose(), input.horarioAlvo());
		return alarmeRepositoryGateway.salvar(alarme);
	}
}
