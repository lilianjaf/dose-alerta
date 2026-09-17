package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.time.Instant;

/**
 * Registra a confirmação do paciente (botão "CONFIRMAR" no WhatsApp ou dígito de
 * confirmação na ligação de voz), encerrando o escalonamento do alarme correspondente.
 */
public class RegistrarConfirmacaoUseCase {

	private static final int MAX_TENTATIVAS = 3;

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;

	public RegistrarConfirmacaoUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
	}

	/**
	 * Reler + reaplicar + salvar é repetido em caso de {@link ConflitoConcorrenciaException}
	 * porque este alarme também é escrito pelo job de escalonamento (ver
	 * {@code AlarmeJpaEntity.version}) — a janela de conflito é curta, então uma nova
	 * tentativa com o estado mais recente do banco normalmente resolve.
	 */
	public Alarme executar(String telefone) {
		for (int tentativa = 1; ; tentativa++) {
			try {
				Alarme alarme = alarmeRepositoryGateway
						.buscarPendenteMaisRecentePorTelefone(telefone)
						.orElseThrow(() -> new AlarmePendenteNaoEncontradoException(telefone));
				alarme.confirmar(Instant.now());
				return alarmeRepositoryGateway.salvar(alarme);
			} catch (ConflitoConcorrenciaException e) {
				if (tentativa >= MAX_TENTATIVAS) {
					throw e;
				}
			}
		}
	}
}
