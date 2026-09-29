package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.rules.RegraStatusLigacao;
import com.dosealerta.mensageria.core.rules.statusligacao.StatusLigacaoContext;
import com.dosealerta.mensageria.core.rules.statusligacao.ValidadorStatusLigacaoRule;
import java.util.List;

public class ProcessarStatusLigacaoUseCaseImpl implements ProcessarStatusLigacaoUseCase {

	private static final String MENSAGEM_FALHA_REPASSE =
			"Falha ao repassar atendimento de ligação do paciente {} ao modulo-scheduler";

	private final AlarmeClientGateway alarmeClientGateway;
	private final LogGateway logGateway;
	private final List<ValidadorStatusLigacaoRule> rules;

	public ProcessarStatusLigacaoUseCaseImpl(AlarmeClientGateway alarmeClientGateway, LogGateway logGateway, List<ValidadorStatusLigacaoRule> rules) {
		this.alarmeClientGateway = alarmeClientGateway;
		this.logGateway = logGateway;
		this.rules = rules;
	}

	@Override
	public void executar(String telefone, String callStatus) {
		try {
			StatusLigacaoContext context = new StatusLigacaoContext(telefone, callStatus);
			rules.forEach(rule -> rule.validar(context));
			repassarAtendimento(telefone, callStatus);
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_REPASSE, telefone, e);
		}
	}

	private void repassarAtendimento(String telefone, String callStatus) {
		if (RegraStatusLigacao.foiAtendida(callStatus)) {
			alarmeClientGateway.registrarLigacaoAtendida(telefone);
		}
	}
}
