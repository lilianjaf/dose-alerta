package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
class MdcCorrelacaoGateway implements CorrelacaoGateway {

	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	@Override
	public String atual() {
		String doContexto = MDC.get(MDC_CORRELATION_ID_KEY);
		return doContexto != null ? doContexto : UUID.randomUUID().toString();
	}

	@Override
	public void executarCom(String correlationId, Runnable acao) {
		MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
		try {
			acao.run();
		} finally {
			MDC.remove(MDC_CORRELATION_ID_KEY);
		}
	}
}
