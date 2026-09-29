package com.dosealerta.notificacao.core.usecase;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.gateway.CorrelacaoGateway;
import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitacaoEnvioContext;
import com.dosealerta.notificacao.core.rules.solicitarenvio.ValidadorSolicitacaoEnvioRule;
import java.time.Clock;
import java.util.List;

public class SolicitarEnvioUseCaseImpl implements SolicitarEnvioUseCase {

	private final EstrategiaCanalGateway estrategiaCanalGateway;
	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;
	private final List<ValidadorSolicitacaoEnvioRule> rules;

	public SolicitarEnvioUseCaseImpl(EstrategiaCanalGateway estrategiaCanalGateway, OutboxEventRepositoryGateway outboxEventRepositoryGateway, CorrelacaoGateway correlacaoGateway, Clock clock, List<ValidadorSolicitacaoEnvioRule> rules) {
		this.estrategiaCanalGateway = estrategiaCanalGateway;
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public void executar(SolicitarEnvioInput input) {
		SolicitacaoEnvioContext context = new SolicitacaoEnvioContext(input);
		rules.forEach(rule -> rule.validar(context));

		Canal canal = estrategiaCanalGateway.resolverCanal(input.etapa());
		OutboxEvent evento = OutboxEvent.novo(
				input.alarmeId(),
				input.pacienteId(),
				input.telefone(),
				input.medicamento(),
				input.dose(),
				input.etapa(),
				canal,
				clock.instant(),
				correlacaoGateway.atual());
		outboxEventRepositoryGateway.salvar(evento);
	}
}
