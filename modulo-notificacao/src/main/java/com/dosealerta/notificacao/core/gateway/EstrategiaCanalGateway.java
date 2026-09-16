package com.dosealerta.notificacao.core.gateway;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;

public interface EstrategiaCanalGateway {

	Canal resolverCanal(EtapaEscalonamento etapa);
}
