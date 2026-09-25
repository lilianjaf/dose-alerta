package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.rules.RegraStatusLigacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessarStatusLigacaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(ProcessarStatusLigacaoUseCase.class);

	private final AlarmeClientGateway alarmeClientGateway;

	public ProcessarStatusLigacaoUseCase(AlarmeClientGateway alarmeClientGateway) {
		this.alarmeClientGateway = alarmeClientGateway;
	}

	public void executar(String telefone, String callStatus) {
		if (telefone == null || telefone.isBlank() || !RegraStatusLigacao.foiAtendida(callStatus)) {
			return;
		}
		try {
			alarmeClientGateway.registrarLigacaoAtendida(telefone);
		} catch (RuntimeException e) {
			log.warn("Falha ao repassar atendimento de ligação do paciente {} ao modulo-scheduler", telefone, e);
		}
	}
}
