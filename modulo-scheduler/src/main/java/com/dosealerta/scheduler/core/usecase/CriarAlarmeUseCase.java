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

	/**
	 * Idempotente: se o paciente já tem um alarme pendente do mesmo medicamento (nome, sem diferenciar
	 * maiúsculas), devolve o existente em vez de duplicar. Quem publica com retentativa (outbox do modulo-ia)
	 * pode chamar mais de uma vez sem criar alarmes repetidos.
	 */
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
