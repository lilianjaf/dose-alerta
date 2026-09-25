package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.rules.DecisaoEscalonamento;
import com.dosealerta.scheduler.core.rules.RegraEscalonamentoAlarme;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EscalonarAlarmesUseCase {

	private static final Logger log = LoggerFactory.getLogger(EscalonarAlarmesUseCase.class);

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final MetricasAlarmeGateway metricasAlarmeGateway;

	public EscalonarAlarmesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway, MetricasAlarmeGateway metricasAlarmeGateway) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.metricasAlarmeGateway = metricasAlarmeGateway;
	}

	public void executar(Instant agora) {
		for (Alarme alarme : alarmeRepositoryGateway.buscarPendentesParaEscalonamento()) {
			try {
				escalonar(alarme, agora);
			} catch (RuntimeException e) {
				log.warn("Falha ao escalonar o alarme {}, será retentado no próximo ciclo", alarme.getId(), e);
			}
		}
	}

	private void escalonar(Alarme alarme, Instant agora) {
		DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, agora);

		if (decisao instanceof DecisaoEscalonamento.Enviar enviar) {
			alarme.registrarEnvio(enviar.etapa(), agora);
			alarmeRepositoryGateway.salvar(alarme);
		} else if (decisao instanceof DecisaoEscalonamento.FinalizarSemConfirmacao) {
			alarme.marcarNaoConfirmado(agora);
			alarmeRepositoryGateway.salvar(alarme);
			metricasAlarmeGateway.registrarNaoConfirmacao();
		}
	}
}
