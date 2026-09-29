package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.rules.RegraRespostaPaciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessarRespostaMensagemUseCase {

	private static final Logger log = LoggerFactory.getLogger(ProcessarRespostaMensagemUseCase.class);

	private final AlarmeClientGateway alarmeClientGateway;

	public ProcessarRespostaMensagemUseCase(AlarmeClientGateway alarmeClientGateway) {
		this.alarmeClientGateway = alarmeClientGateway;
	}

	// Devolve se realmente confirmou algo: quem chama usa isso pra decidir se manda um "recebido" de volta ao
	// paciente, em vez de confirmar de boca cheia algo que pode não ter encontrado nenhum alarme.
	public boolean executar(String telefone, String corpo, String textoBotao) {
		if (!RegraRespostaPaciente.ehConfirmacao(corpo, textoBotao)) {
			return false;
		}
		try {
			alarmeClientGateway.registrarConfirmacao(telefone);
			return true;
		} catch (RuntimeException e) {
			log.warn("Falha ao repassar confirmação do paciente {} ao modulo-scheduler", telefone, e);
			return false;
		}
	}
}
