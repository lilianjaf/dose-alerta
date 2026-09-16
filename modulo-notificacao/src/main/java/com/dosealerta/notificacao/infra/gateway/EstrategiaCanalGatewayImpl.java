package com.dosealerta.notificacao.infra.gateway;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.rules.RegraEstrategiaCanal;
import org.springframework.stereotype.Component;

@Component
class EstrategiaCanalGatewayImpl implements EstrategiaCanalGateway {

	@Override
	public Canal resolverCanal(EtapaEscalonamento etapa) {
		return RegraEstrategiaCanal.decidir(etapa);
	}
}
