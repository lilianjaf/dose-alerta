package com.dosealerta.scheduler.core.gateway;

import com.dosealerta.scheduler.core.domain.EventoInteracao;

/**
 * Aciona o modulo-relatorio-adesao para registrar uma interação do paciente
 * (confirmação, não confirmação, atendimento de ligação) no read model de adesão.
 */
public interface RelatorioAdesaoClientGateway {

	void registrarInteracao(EventoInteracao evento);
}
