package com.dosealerta.notificacao.infra.gateway;

import com.dosealerta.notificacao.core.gateway.LogGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class Slf4jLogGateway implements LogGateway {

	private static final Logger log = LoggerFactory.getLogger(Slf4jLogGateway.class);

	@Override
	public void aviso(String mensagem, Object... argumentos) {
		log.warn(mensagem, argumentos);
	}

	@Override
	public void erro(String mensagem, Object... argumentos) {
		log.error(mensagem, argumentos);
	}
}
