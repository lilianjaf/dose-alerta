package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;

public class ConfirmarReceitaPorTelefoneUseCase {

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final ConfirmarReceitaUseCase confirmarReceitaUseCase;

	public ConfirmarReceitaPorTelefoneUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway, ConfirmarReceitaUseCase confirmarReceitaUseCase) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.confirmarReceitaUseCase = confirmarReceitaUseCase;
	}

	public Receita executar(String telefone, ConfirmarReceitaInput correcoes) {
		Receita pendente = receitaRepositoryGateway
				.buscarAguardandoConfirmacaoMaisRecentePorTelefone(telefone)
				.orElseThrow(() -> new ReceitaNaoEncontradaException(telefone));
		return confirmarReceitaUseCase.executar(pendente.getId(), correcoes);
	}
}
