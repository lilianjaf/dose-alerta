package com.dosealerta.relatorioadesao.core.usecase;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistroInteracaoContext;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.ValidadorRegistroInteracaoRule;
import java.util.List;

public class RegistrarInteracaoUseCaseImpl implements RegistrarInteracaoUseCase {

	private final InteracaoRepositoryGateway interacaoRepositoryGateway;
	private final List<ValidadorRegistroInteracaoRule> rules;

	public RegistrarInteracaoUseCaseImpl(InteracaoRepositoryGateway interacaoRepositoryGateway, List<ValidadorRegistroInteracaoRule> rules) {
		this.interacaoRepositoryGateway = interacaoRepositoryGateway;
		this.rules = rules;
	}

	@Override
	public void executar(RegistrarInteracaoInput input) {
		RegistroInteracaoContext context = new RegistroInteracaoContext(input);
		rules.forEach(rule -> rule.validar(context));

		interacaoRepositoryGateway.salvar(
				Interacao.nova(input.id(), input.pacienteId(), input.medicamento(), input.tipo(), input.registradaEm()));
	}
}
