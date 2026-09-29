package com.dosealerta.scheduler.infra.decorator;

import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEscalonarAlarmesUseCase implements EscalonarAlarmesUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingEscalonarAlarmesUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando EscalonarAlarmes";
	private static final String MENSAGEM_SUCESSO = "EscalonarAlarmes concluído";
	private static final String MENSAGEM_FALHA = "EscalonarAlarmes falhou: {}";

	private final EscalonarAlarmesUseCase delegate;

	public LoggingEscalonarAlarmesUseCase(EscalonarAlarmesUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public void executar() {
		log.info(MENSAGEM_INICIO);
		try {
			delegate.executar();
			log.info(MENSAGEM_SUCESSO);
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
