package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.rules.RegraRespostaPaciente;
import com.dosealerta.mensageria.core.rules.respostamensagem.RespostaMensagemContext;
import com.dosealerta.mensageria.core.rules.respostamensagem.ValidadorRespostaMensagemRule;
import java.util.List;

public class ProcessarRespostaMensagemUseCaseImpl implements ProcessarRespostaMensagemUseCase {

	private static final String MENSAGEM_FALHA_REPASSE =
			"Falha ao repassar confirmação do paciente {} ao modulo-scheduler";

	private final AlarmeClientGateway alarmeClientGateway;
	private final LogGateway logGateway;
	private final List<ValidadorRespostaMensagemRule> rules;

	public ProcessarRespostaMensagemUseCaseImpl(AlarmeClientGateway alarmeClientGateway, LogGateway logGateway, List<ValidadorRespostaMensagemRule> rules) {
		this.alarmeClientGateway = alarmeClientGateway;
		this.logGateway = logGateway;
		this.rules = rules;
	}

	@Override
	public boolean executar(String telefone, String corpo, String textoBotao) {
		try {
			RespostaMensagemContext context = new RespostaMensagemContext(telefone, corpo, textoBotao);
			rules.forEach(rule -> rule.validar(context));
			return repassarConfirmacao(telefone, corpo, textoBotao);
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_REPASSE, telefone, e);
			return false;
		}
	}

	private boolean repassarConfirmacao(String telefone, String corpo, String textoBotao) {
		if (!RegraRespostaPaciente.ehConfirmacao(corpo, textoBotao)) {
			return false;
		}
		alarmeClientGateway.registrarConfirmacao(telefone);
		return true;
	}
}
