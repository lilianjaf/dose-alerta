package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import java.time.Instant;

public class RegistrarConfirmacaoUseCase {

	private static final int MAX_TENTATIVAS = 3;

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final MetricasAlarmeGateway metricasAlarmeGateway;

	public RegistrarConfirmacaoUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway, MetricasAlarmeGateway metricasAlarmeGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.metricasAlarmeGateway = metricasAlarmeGateway;
	}

	public Alarme executar(String telefone) {
		for (int tentativa = 1; ; tentativa++) {
			try {
				Alarme alarme = alarmeRepositoryGateway
						.buscarPendenteMaisRecentePorTelefone(telefone)
						.orElseThrow(() -> new AlarmePendenteNaoEncontradoException(telefone));
				alarme.confirmar(Instant.now());
				Alarme salvo = alarmeRepositoryGateway.salvar(alarme);
				metricasAlarmeGateway.registrarConfirmacao();
				return salvo;
			} catch (ConflitoConcorrenciaException e) {
				if (tentativa >= MAX_TENTATIVAS) {
					throw e;
				}
			}
		}
	}
}
