package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistroConfirmacaoContext;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.ValidadorRegistroConfirmacaoRule;
import java.time.Clock;
import java.util.List;

public class RegistrarConfirmacaoUseCaseImpl implements RegistrarConfirmacaoUseCase {

	private static final int MAX_TENTATIVAS = 3;

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final MetricasAlarmeGateway metricasAlarmeGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;
	private final List<ValidadorRegistroConfirmacaoRule> rules;

	public RegistrarConfirmacaoUseCaseImpl(AlarmeRepositoryGateway alarmeRepositoryGateway, MetricasAlarmeGateway metricasAlarmeGateway, CorrelacaoGateway correlacaoGateway, Clock clock, List<ValidadorRegistroConfirmacaoRule> rules) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.metricasAlarmeGateway = metricasAlarmeGateway;
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
		RegistroConfirmacaoContext context = new RegistroConfirmacaoContext(telefone, alarme);
		rules.forEach(rule -> rule.validar(context));

		alarme.confirmar(clock.instant(), correlacaoGateway.atual());
		Alarme salvo = alarmeRepositoryGateway.salvar(alarme);
		metricasAlarmeGateway.registrarConfirmacao();
		return salvo;
	}

	private Alarme buscarPendente(String telefone) {
		if (telefone == null || telefone.isBlank()) {
			return null;
		}
		return alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone(telefone).orElse(null);
	}
}
