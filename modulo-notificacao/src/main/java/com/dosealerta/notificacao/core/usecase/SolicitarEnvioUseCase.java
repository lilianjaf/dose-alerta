package com.dosealerta.notificacao.core.usecase;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;

public class SolicitarEnvioUseCase {

	private final EstrategiaCanalGateway estrategiaCanalGateway;
	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	public SolicitarEnvioUseCase(
			EstrategiaCanalGateway estrategiaCanalGateway, OutboxEventRepositoryGateway outboxEventRepositoryGateway) {
		this.estrategiaCanalGateway = estrategiaCanalGateway;
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
	}

	public void executar(SolicitarEnvioInput input) {
		Canal canal = estrategiaCanalGateway.resolverCanal(input.etapa());
		OutboxEvent evento = OutboxEvent.novo(
				input.alarmeId(),
				input.pacienteId(),
				input.telefone(),
				input.medicamento(),
				input.dose(),
				input.etapa(),
				canal,
				Instant.now());
		outboxEventRepositoryGateway.salvar(evento);
	}
}
