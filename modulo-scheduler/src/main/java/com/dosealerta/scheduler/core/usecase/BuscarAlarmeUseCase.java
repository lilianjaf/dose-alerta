package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.AlarmeNaoEncontradoException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.util.UUID;

public class BuscarAlarmeUseCase {

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;

	public BuscarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
	}

	public Alarme executar(UUID id) {
		return alarmeRepositoryGateway.buscarPorId(id).orElseThrow(() -> new AlarmeNaoEncontradoException(id));
	}
}
