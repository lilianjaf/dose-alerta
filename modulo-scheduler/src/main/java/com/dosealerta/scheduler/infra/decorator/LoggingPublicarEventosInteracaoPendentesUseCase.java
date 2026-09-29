package com.dosealerta.scheduler.infra.decorator;

import com.dosealerta.scheduler.core.usecase.PublicarEventosInteracaoPendentesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPublicarEventosInteracaoPendentesUseCase implements PublicarEventosInteracaoPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingPublicarEventosInteracaoPendentesUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando PublicarEventosInteracaoPendentes";
	private static final String MENSAGEM_SUCESSO = "PublicarEventosInteracaoPendentes concluído";
	private static final String MENSAGEM_FALHA = "PublicarEventosInteracaoPendentes falhou: {}";

	private final PublicarEventosInteracaoPendentesUseCase delegate;

	public LoggingPublicarEventosInteracaoPendentesUseCase(PublicarEventosInteracaoPendentesUseCase delegate) {
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
