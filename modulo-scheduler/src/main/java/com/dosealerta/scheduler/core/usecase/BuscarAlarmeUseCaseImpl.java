package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.rules.buscar.BuscaAlarmeContext;
import com.dosealerta.scheduler.core.rules.buscar.ValidadorBuscaAlarmeRule;
import java.util.List;
import java.util.UUID;

public class BuscarAlarmeUseCaseImpl implements BuscarAlarmeUseCase {

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final List<ValidadorBuscaAlarmeRule> rules;

	public BuscarAlarmeUseCaseImpl(AlarmeRepositoryGateway alarmeRepositoryGateway, List<ValidadorBuscaAlarmeRule> rules) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.rules = rules;
	}

	@Override
	public Alarme executar(UUID id) {
		Alarme alarme = id == null ? null : alarmeRepositoryGateway.buscarPorId(id).orElse(null);
		BuscaAlarmeContext context = new BuscaAlarmeContext(id, alarme);
		rules.forEach(rule -> rule.validar(context));
		return alarme;
	}
}
