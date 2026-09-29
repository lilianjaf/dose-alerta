package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.rules.DecisaoEscalonamento;
import com.dosealerta.scheduler.core.rules.RegraEscalonamentoAlarme;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class EscalonarAlarmesUseCaseImpl implements EscalonarAlarmesUseCase {

	private static final String MENSAGEM_FALHA_ESCALONAR = "Falha ao escalonar o alarme {}, será retentado no próximo ciclo";

	private final AlarmeRepositoryGateway alarmeRepositoryGateway;
	private final MetricasAlarmeGateway metricasAlarmeGateway;
	private final LogGateway logGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;
	private final Duration intervaloEntreEtapas;

	public EscalonarAlarmesUseCaseImpl(AlarmeRepositoryGateway alarmeRepositoryGateway, MetricasAlarmeGateway metricasAlarmeGateway, LogGateway logGateway, CorrelacaoGateway correlacaoGateway, Clock clock, Duration intervaloEntreEtapas) {
		this.alarmeRepositoryGateway = alarmeRepositoryGateway;
		this.metricasAlarmeGateway = metricasAlarmeGateway;
		this.logGateway = logGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
		this.intervaloEntreEtapas = intervaloEntreEtapas;
	}

	@Override
	public void executar() {
		Instant agora = clock.instant();
		alarmeRepositoryGateway.buscarPendentesParaEscalonamento().forEach(alarme -> escalonarComTolerancia(alarme, agora));
	}

	private void escalonarComTolerancia(Alarme alarme, Instant agora) {
		try {
			escalonar(alarme, agora);
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_ESCALONAR, alarme.getId(), e);
		}
	}

	private void escalonar(Alarme alarme, Instant agora) {
		DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, agora, intervaloEntreEtapas);
		if (decisao instanceof DecisaoEscalonamento.Enviar enviar) {
			alarme.registrarEnvio(enviar.etapa(), agora, correlacaoGateway.atual());
			alarmeRepositoryGateway.salvar(alarme);
		} else if (decisao instanceof DecisaoEscalonamento.FinalizarSemConfirmacao) {
			alarme.marcarNaoConfirmado(agora, correlacaoGateway.atual());
			alarmeRepositoryGateway.salvar(alarme);
			metricasAlarmeGateway.registrarNaoConfirmacao();
		}
	}
}
