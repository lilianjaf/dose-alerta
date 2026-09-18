package com.dosealerta.relatorioadesao.core.usecase;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;

/**
 * Recebe o {@code InteracaoRegistradaEvent} publicado pelo modulo-scheduler (Etapa 8.1) e
 * grava no read model de adesão.
 */
public class RegistrarInteracaoUseCase {

	private final InteracaoRepositoryGateway interacaoRepositoryGateway;

	public RegistrarInteracaoUseCase(InteracaoRepositoryGateway interacaoRepositoryGateway) {
		this.interacaoRepositoryGateway = interacaoRepositoryGateway;
	}

	public void executar(RegistrarInteracaoInput input) {
		interacaoRepositoryGateway.salvar(
				Interacao.nova(input.id(), input.pacienteId(), input.medicamento(), input.tipo(), input.registradaEm()));
	}
}
