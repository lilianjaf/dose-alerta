package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;

public class CriarAlarmeUseCase {

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;

	public CriarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
	}

	public ResultadoCriarAlarme executar(CriarAlarmeInput input) {
		String medicamento = input.medicamento().trim();

		return alarmeRepositoryGateway
				.buscarPendentePorPacienteEMedicamento(input.pacienteId(), medicamento)
				.map(existente -> new ResultadoCriarAlarme(existente, true))
				.orElseGet(() -> {
					Alarme alarme = Alarme.criar(
							input.pacienteId(), input.telefone(), medicamento, input.dose(), input.horarioAlvo());
					return new ResultadoCriarAlarme(alarmeRepositoryGateway.salvar(alarme), false);
				});
	}
}
