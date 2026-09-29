package com.dosealerta.notificacao.infra.decorator;

import com.dosealerta.notificacao.core.usecase.PublicarEventosPendentesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPublicarEventosPendentesUseCase implements PublicarEventosPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingPublicarEventosPendentesUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando PublicarEventosPendentes";
	private static final String MENSAGEM_SUCESSO = "PublicarEventosPendentes concluído";
	private static final String MENSAGEM_FALHA = "PublicarEventosPendentes falhou: {}";

	private final PublicarEventosPendentesUseCase delegate;

	public LoggingPublicarEventosPendentesUseCase(PublicarEventosPendentesUseCase delegate) {
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
