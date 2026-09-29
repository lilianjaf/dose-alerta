package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.rules.criar.CriacaoAlarmeContext;
import com.dosealerta.scheduler.core.rules.criar.ValidadorCriacaoAlarmeRule;
import java.time.Clock;
import java.util.List;

public class CriarAlarmeUseCaseImpl implements CriarAlarmeUseCase {

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final Clock clock;
	private final List<ValidadorCriacaoAlarmeRule> rules;

	public CriarAlarmeUseCaseImpl(AlarmeRepositoryGateway alarmeRepositoryGateway, Clock clock, List<ValidadorCriacaoAlarmeRule> rules) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public ResultadoCriarAlarme executar(CriarAlarmeInput input) {
		CriacaoAlarmeContext context = new CriacaoAlarmeContext(input);
		rules.forEach(rule -> rule.validar(context));

		String medicamento = input.medicamento().trim();
		return alarmeRepositoryGateway
				.buscarPendentePorPacienteEMedicamento(input.pacienteId(), medicamento)
				.map(existente -> new ResultadoCriarAlarme(existente, true))
				.orElseGet(() -> criar(input, medicamento));
	}

	private ResultadoCriarAlarme criar(CriarAlarmeInput input, String medicamento) {
		Alarme alarme = Alarme.criar(
				input.pacienteId(), input.telefone(), medicamento, input.dose(), input.horarioAlvo(), clock.instant());
		return new ResultadoCriarAlarme(alarmeRepositoryGateway.salvar(alarme), false);
	}
}
