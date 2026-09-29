package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.rules.buscar.BuscaReceitaContext;
import com.dosealerta.ia.core.rules.buscar.ValidadorBuscaReceitaRule;
import java.util.List;
import java.util.UUID;

public class BuscarReceitaUseCaseImpl implements BuscarReceitaUseCase {

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final List<ValidadorBuscaReceitaRule> rules;

	public BuscarReceitaUseCaseImpl(ReceitaRepositoryGateway receitaRepositoryGateway, List<ValidadorBuscaReceitaRule> rules) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.rules = rules;
	}

	@Override
	public Receita executar(UUID id) {
		Receita receita = id == null ? null : receitaRepositoryGateway.buscarPorId(id).orElse(null);
		BuscaReceitaContext context = new BuscaReceitaContext(id, receita);
		rules.forEach(rule -> rule.validar(context));
		return receita;
	}
}
