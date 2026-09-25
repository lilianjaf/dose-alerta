package com.dosealerta.notificacao.core.rules;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

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
