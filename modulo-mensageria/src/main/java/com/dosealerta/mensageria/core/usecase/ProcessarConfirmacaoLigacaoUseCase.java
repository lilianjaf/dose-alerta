package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;
import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processa o dígito capturado durante a ligação de confirmação e, quando corresponde ao
 * dígito de confirmação, repassa ao modulo-scheduler. Retorna se houve confirmação para que
 * o controller monte a resposta falada (TwiML) adequada ao paciente.
 */
public class ProcessarConfirmacaoLigacaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(ProcessarConfirmacaoLigacaoUseCase.class);

	private final AlarmeClientGateway alarmeClientGateway;

	public ProcessarConfirmacaoLigacaoUseCase(AlarmeClientGateway alarmeClientGateway) {
		this.alarmeClientGateway = alarmeClientGateway;
	}

	public boolean executar(String telefone, String digitos) {
		boolean confirmado = SolicitacaoLigacao.DIGITO_CONFIRMACAO.equals(digitos);
		if (confirmado) {
			try {
				alarmeClientGateway.registrarConfirmacao(telefone);
			} catch (RuntimeException e) {
				log.warn("Falha ao repassar confirmação por ligação do paciente {} ao modulo-scheduler", telefone, e);
			}
		}
		return confirmado;
	}
}
