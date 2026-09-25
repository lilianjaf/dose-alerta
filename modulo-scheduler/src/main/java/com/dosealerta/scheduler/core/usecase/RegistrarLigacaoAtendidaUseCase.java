package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.time.Instant;

public class RegistrarLigacaoAtendidaUseCase {

	private static final int MAX_TENTATIVAS = 3;

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;

	public RegistrarLigacaoAtendidaUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
	}

	public Alarme executar(String telefone) {
		for (int tentativa = 1; ; tentativa++) {
			try {
				Alarme alarme = alarmeRepositoryGateway
						.buscarPendenteMaisRecentePorTelefone(telefone)
						.orElseThrow(() -> new AlarmePendenteNaoEncontradoException(telefone));
				alarme.registrarLigacaoAtendida(Instant.now());
				return alarmeRepositoryGateway.salvar(alarme);
			} catch (ConflitoConcorrenciaException e) {
				if (tentativa >= MAX_TENTATIVAS) {
					throw e;
				}
			}
		}
	}
}
