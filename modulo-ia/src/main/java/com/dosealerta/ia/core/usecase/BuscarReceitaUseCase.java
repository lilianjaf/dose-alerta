package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.util.UUID;

public class BuscarReceitaUseCase {

	private final ReceitaRepositoryGateway receitaRepositoryGateway;

	public BuscarReceitaUseCase(ReceitaRepositoryGateway receitaRepositoryGateway) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
	}

	public Receita executar(UUID id) {
		return receitaRepositoryGateway.buscarPorId(id).orElseThrow(() -> new ReceitaNaoEncontradaException(id));
	}
}
