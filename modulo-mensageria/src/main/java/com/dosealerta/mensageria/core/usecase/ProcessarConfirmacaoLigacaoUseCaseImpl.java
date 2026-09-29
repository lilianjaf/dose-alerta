package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;
import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.rules.confirmacaoligacao.ConfirmacaoLigacaoContext;
import com.dosealerta.mensageria.core.rules.confirmacaoligacao.ValidadorConfirmacaoLigacaoRule;
import java.util.List;

public class ProcessarConfirmacaoLigacaoUseCaseImpl implements ProcessarConfirmacaoLigacaoUseCase {

	private static final String MENSAGEM_FALHA_REPASSE =
			"Falha ao repassar confirmação por ligação do paciente {} ao modulo-scheduler";
	private static final String MENSAGEM_FALHA_VALIDACAO =
			"Confirmação por ligação descartada para o paciente {}";

	private final AlarmeClientGateway alarmeClientGateway;
	private final LogGateway logGateway;
	private final List<ValidadorConfirmacaoLigacaoRule> rules;

	public ProcessarConfirmacaoLigacaoUseCaseImpl(AlarmeClientGateway alarmeClientGateway, LogGateway logGateway, List<ValidadorConfirmacaoLigacaoRule> rules) {
		this.alarmeClientGateway = alarmeClientGateway;
		this.logGateway = logGateway;
		this.rules = rules;
	}

	@Override
	public boolean executar(String telefone, String digitos) {
		try {
			ConfirmacaoLigacaoContext context = new ConfirmacaoLigacaoContext(telefone, digitos);
			rules.forEach(rule -> rule.validar(context));
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_VALIDACAO, telefone, e);
			return false;
		}
		boolean confirmado = SolicitacaoLigacao.DIGITO_CONFIRMACAO.equals(digitos);
		if (confirmado) {
			repassarConfirmacao(telefone);
		}
		return confirmado;
	}

	private void repassarConfirmacao(String telefone) {
		try {
			alarmeClientGateway.registrarConfirmacao(telefone);
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_REPASSE, telefone, e);
		}
	}
}
