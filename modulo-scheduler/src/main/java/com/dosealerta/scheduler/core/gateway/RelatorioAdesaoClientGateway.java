package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.EventoInteracao;

public interface RelatorioAdesaoClientGateway {

	void registrarInteracao(EventoInteracao evento);
}
