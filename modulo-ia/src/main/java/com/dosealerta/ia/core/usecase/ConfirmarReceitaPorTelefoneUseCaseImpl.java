package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.rules.confirmarportelefone.ConfirmacaoPorTelefoneContext;
import com.dosealerta.ia.core.rules.confirmarportelefone.ValidadorConfirmacaoPorTelefoneRule;
import java.util.List;

public class ConfirmarReceitaPorTelefoneUseCaseImpl implements ConfirmarReceitaPorTelefoneUseCase {

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final ConfirmarReceitaUseCase confirmarReceitaUseCase;
	private final List<ValidadorConfirmacaoPorTelefoneRule> rules;

	public ConfirmarReceitaPorTelefoneUseCaseImpl(ReceitaRepositoryGateway receitaRepositoryGateway, ConfirmarReceitaUseCase confirmarReceitaUseCase, List<ValidadorConfirmacaoPorTelefoneRule> rules) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.confirmarReceitaUseCase = confirmarReceitaUseCase;
		this.rules = rules;
	}

	@Override
	public Receita executar(String telefone, ConfirmarReceitaInput correcoes) {
		Receita pendente = buscarPendente(telefone);
		ConfirmacaoPorTelefoneContext context = new ConfirmacaoPorTelefoneContext(telefone, pendente);
		rules.forEach(rule -> rule.validar(context));

		return confirmarReceitaUseCase.executar(pendente.getId(), correcoes);
	}

	private Receita buscarPendente(String telefone) {
		if (telefone == null || telefone.isBlank()) {
			return null;
		}
		return receitaRepositoryGateway.buscarAguardandoConfirmacaoMaisRecentePorTelefone(telefone).orElse(null);
	}
}
