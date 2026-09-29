package com.dosealerta.scheduler.core.rules;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import java.time.Duration;
import java.time.Instant;

public final class RegraEscalonamentoAlarme {

	public static final Duration INTERVALO_ENTRE_ETAPAS_PADRAO = Duration.ofMinutes(15);

	private RegraEscalonamentoAlarme() {
	}

	public static DecisaoEscalonamento decidir(Alarme alarme, Instant agora) {
		return decidir(alarme, agora, INTERVALO_ENTRE_ETAPAS_PADRAO);
	}

	public static DecisaoEscalonamento decidir(Alarme alarme, Instant agora, Duration intervaloEntreEtapas) {
		if (alarme.getStatus() != StatusAlarme.PENDENTE) {
			return new DecisaoEscalonamento.Nada();
		}

		EtapaEscalonamento etapaAtual = alarme.getEtapaAtual();
		if (etapaAtual == null) {
			return Duration.between(alarme.getHorarioAlvo(), agora).isNegative()
					? new DecisaoEscalonamento.Nada()
					: new DecisaoEscalonamento.Enviar(EtapaEscalonamento.LEMBRETE_INICIAL);
		}

		Duration decorridoDesdeUltimoEnvio = Duration.between(alarme.getUltimoEnvioEm(), agora);
		if (decorridoDesdeUltimoEnvio.compareTo(intervaloEntreEtapas) < 0) {
			return new DecisaoEscalonamento.Nada();
		}

		return switch (etapaAtual) {
			case LEMBRETE_INICIAL -> new DecisaoEscalonamento.Enviar(EtapaEscalonamento.REFORCO);
			case REFORCO -> new DecisaoEscalonamento.Enviar(EtapaEscalonamento.LIGACAO);
			case LIGACAO -> new DecisaoEscalonamento.FinalizarSemConfirmacao();
		};
	}
}
