package com.dosealerta.notificacao.core.gateway;

import com.dosealerta.notificacao.core.domain.OutboxEvent;

public interface MensageriaClientGateway {

	void enviar(OutboxEvent evento);
}
