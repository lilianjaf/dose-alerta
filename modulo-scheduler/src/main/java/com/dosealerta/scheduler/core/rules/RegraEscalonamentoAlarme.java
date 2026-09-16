package com.dosealerta.scheduler.core.rules;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import java.time.Duration;
import java.time.Instant;

/**
 * Decide, a partir do horário alvo e da etapa já enviada, qual a próxima ação de
 * escalonamento de um alarme (T+0 lembrete, +15min reforço, +15min ligação, +15min desiste).
 *
 * <p>Os intervalos entre etapas são contados a partir do último envio efetivo
 * ({@link Alarme#getUltimoEnvioEm()}), não do horário alvo original — assim, se o
 * escalonamento ficar atrasado (ex: scheduler fora do ar), as etapas seguintes continuam
 * espaçadas por {@link #INTERVALO_ENTRE_ETAPAS} em vez de disparar todas de uma vez.
 */
public final class RegraEscalonamentoAlarme {

	static final Duration INTERVALO_ENTRE_ETAPAS = Duration.ofMinutes(15);

	private RegraEscalonamentoAlarme() {
	}

	public static DecisaoEscalonamento decidir(Alarme alarme, Instant agora) {
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
		if (decorridoDesdeUltimoEnvio.compareTo(INTERVALO_ENTRE_ETAPAS) < 0) {
			return new DecisaoEscalonamento.Nada();
		}

		return switch (etapaAtual) {
			case LEMBRETE_INICIAL -> new DecisaoEscalonamento.Enviar(EtapaEscalonamento.REFORCO);
			case REFORCO -> new DecisaoEscalonamento.Enviar(EtapaEscalonamento.LIGACAO);
			case LIGACAO -> new DecisaoEscalonamento.FinalizarSemConfirmacao();
		};
	}
}
