package com.dosealerta.scheduler.core.rules;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import java.time.Duration;
import java.time.Instant;

/**
 * Decide, a partir do horário alvo e da etapa já enviada, qual a próxima ação de
 * escalonamento de um alarme (T+0, T+15min reforço, T+30min ligação, T+45min desiste).
 */
public final class RegraEscalonamentoAlarme {

	static final Duration ATRASO_REFORCO = Duration.ofMinutes(15);
	static final Duration ATRASO_LIGACAO = Duration.ofMinutes(30);
	static final Duration ATRASO_FINALIZACAO = Duration.ofMinutes(45);

	private RegraEscalonamentoAlarme() {
	}

	public static DecisaoEscalonamento decidir(Alarme alarme, Instant agora) {
		if (alarme.getStatus() != StatusAlarme.PENDENTE) {
			return new DecisaoEscalonamento.Nada();
		}

		Duration decorrido = Duration.between(alarme.getHorarioAlvo(), agora);
		if (decorrido.isNegative()) {
			return new DecisaoEscalonamento.Nada();
		}

		EtapaEscalonamento etapaAtual = alarme.getEtapaAtual();
		if (etapaAtual == null) {
			return new DecisaoEscalonamento.Enviar(EtapaEscalonamento.LEMBRETE_INICIAL);
		}

		return switch (etapaAtual) {
			case LEMBRETE_INICIAL -> decorrido.compareTo(ATRASO_REFORCO) >= 0
					? new DecisaoEscalonamento.Enviar(EtapaEscalonamento.REFORCO)
					: new DecisaoEscalonamento.Nada();
			case REFORCO -> decorrido.compareTo(ATRASO_LIGACAO) >= 0
					? new DecisaoEscalonamento.Enviar(EtapaEscalonamento.LIGACAO)
					: new DecisaoEscalonamento.Nada();
			case LIGACAO -> decorrido.compareTo(ATRASO_FINALIZACAO) >= 0
					? new DecisaoEscalonamento.FinalizarSemConfirmacao()
					: new DecisaoEscalonamento.Nada();
		};
	}
}
