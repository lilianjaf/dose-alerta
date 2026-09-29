package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.rules.registrarligacao.RegistroLigacaoContext;
import com.dosealerta.scheduler.core.rules.registrarligacao.ValidadorRegistroLigacaoRule;
import java.time.Clock;
import java.util.List;

public class RegistrarLigacaoAtendidaUseCaseImpl implements RegistrarLigacaoAtendidaUseCase {

	private static final int MAX_TENTATIVAS = 3;

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;
	private final List<ValidadorRegistroLigacaoRule> rules;

	public RegistrarLigacaoAtendidaUseCaseImpl(AlarmeRepositoryGateway alarmeRepositoryGateway, CorrelacaoGateway correlacaoGateway, Clock clock, List<ValidadorRegistroLigacaoRule> rules) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public Alarme executar(String telefone) {
		for (int tentativa = 1; ; tentativa++) {
			try {
				return registrar(telefone);
			} catch (ConflitoConcorrenciaException e) {
				if (tentativa >= MAX_TENTATIVAS) {
					throw e;
				}
			}
		}
	}

	private Alarme registrar(String telefone) {
		Alarme alarme = buscarPendente(telefone);
		RegistroLigacaoContext context = new RegistroLigacaoContext(telefone, alarme);
		rules.forEach(rule -> rule.validar(context));

		alarme.registrarLigacaoAtendida(clock.instant(), correlacaoGateway.atual());
		return alarmeRepositoryGateway.salvar(alarme);
	}

	private Alarme buscarPendente(String telefone) {
		if (telefone == null || telefone.isBlank()) {
			return null;
		}
		return alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone(telefone).orElse(null);
	}
}
