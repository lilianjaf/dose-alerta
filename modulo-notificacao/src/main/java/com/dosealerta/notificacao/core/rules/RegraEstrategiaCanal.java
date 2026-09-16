package com.dosealerta.notificacao.core.rules;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

/**
 * Mapeia cada etapa de escalonamento para o canal usado para contatar o paciente: lembrete
 * inicial e reforço são mensagem de WhatsApp, a última etapa é ligação de voz.
 */
public final class RegraEstrategiaCanal {

	private RegraEstrategiaCanal() {
	}

	public static Canal decidir(EtapaEscalonamento etapa) {
		return switch (etapa) {
			case LEMBRETE_INICIAL, REFORCO -> Canal.MENSAGEM;
			case LIGACAO -> Canal.LIGACAO;
		};
	}
}
