package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.rules.RegraRespostaPaciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processa a resposta de texto/botão recebida do WhatsApp e, quando equivale a uma
 * confirmação, repassa ao modulo-scheduler. Falhas ao repassar são apenas logadas — o
 * webhook do Twilio deve sempre ser confirmado (200), nunca falhar por causa de uma
 * indisponibilidade momentânea do modulo-scheduler.
 */
public class ProcessarRespostaMensagemUseCase {

	private static final Logger log = LoggerFactory.getLogger(ProcessarRespostaMensagemUseCase.class);

	private final AlarmeClientGateway alarmeClientGateway;

	public ProcessarRespostaMensagemUseCase(AlarmeClientGateway alarmeClientGateway) {
		this.alarmeClientGateway = alarmeClientGateway;
	}

	public void executar(String telefone, String corpo, String textoBotao) {
		if (!RegraRespostaPaciente.ehConfirmacao(corpo, textoBotao)) {
			return;
		}
		try {
			alarmeClientGateway.registrarConfirmacao(telefone);
		} catch (RuntimeException e) {
			log.warn("Falha ao repassar confirmação do paciente {} ao modulo-scheduler", telefone, e);
		}
	}
}
