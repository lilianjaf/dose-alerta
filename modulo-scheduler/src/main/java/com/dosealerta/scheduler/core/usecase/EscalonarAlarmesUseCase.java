package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.rules.DecisaoEscalonamento;
import com.dosealerta.scheduler.core.rules.RegraEscalonamentoAlarme;
import java.time.Instant;

/**
 * Varre os alarmes pendentes e decide se cada um precisa avançar de etapa (gravando o
 * respectivo evento de outbox) ou ser finalizado por falta de confirmação.
 */
public class EscalonarAlarmesUseCase {

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;

	public EscalonarAlarmesUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
	}

	public void executar(Instant agora) {
		for (Alarme alarme : alarmeRepositoryGateway.buscarPendentesParaEscalonamento()) {
			DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, agora);

			if (decisao instanceof DecisaoEscalonamento.Enviar enviar) {
				alarme.registrarEnvio(enviar.etapa(), agora);
				alarmeRepositoryGateway.salvar(alarme);
			} else if (decisao instanceof DecisaoEscalonamento.FinalizarSemConfirmacao) {
				alarme.marcarNaoConfirmado(agora);
				alarmeRepositoryGateway.salvar(alarme);
			}
		}
	}
}
