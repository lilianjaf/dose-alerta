package com.dosealerta.scheduler.core.rules;

import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;

public sealed interface DecisaoEscalonamento {

	record Enviar(EtapaEscalonamento etapa) implements DecisaoEscalonamento {
	}

	record FinalizarSemConfirmacao() implements DecisaoEscalonamento {
	}

	record Nada() implements DecisaoEscalonamento {
	}
}
