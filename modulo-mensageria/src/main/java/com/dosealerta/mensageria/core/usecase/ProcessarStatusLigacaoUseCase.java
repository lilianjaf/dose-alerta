package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.rules.RegraStatusLigacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processa o status da ligação reportado pelo Twilio e, quando indica que o paciente
 * atendeu, repassa ao modulo-scheduler como uma interação (independente de o paciente ter
 * ou não confirmado a dose durante a chamada).
 */
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
