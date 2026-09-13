package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;

public interface NotificacaoClientGateway {

	void solicitarEnvio(Alarme alarme, EtapaEscalonamento etapa);
}
