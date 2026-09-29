package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.rules.DecisaoEscalonamento;
import com.dosealerta.scheduler.core.rules.RegraEscalonamentoAlarme;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EscalonarAlarmesUseCase {

	private static final Logger log = LoggerFactory.getLogger(EscalonarAlarmesUseCase.class);

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final MetricasAlarmeGateway metricasAlarmeGateway;
	private final Duration intervaloEntreEtapas;

	public EscalonarAlarmesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway, MetricasAlarmeGateway metricasAlarmeGateway) {
		this(alarmeRepositoryGateway, metricasAlarmeGateway, RegraEscalonamentoAlarme.INTERVALO_ENTRE_ETAPAS_PADRAO);
	}

	public EscalonarAlarmesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			MetricasAlarmeGateway metricasAlarmeGateway,
			Duration intervaloEntreEtapas) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.metricasAlarmeGateway = metricasAlarmeGateway;
		this.intervaloEntreEtapas = intervaloEntreEtapas;
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
		DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, agora, intervaloEntreEtapas);

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
