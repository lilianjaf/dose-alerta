package com.dosealerta.notificacao.infra.decorator;

import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.usecase.SolicitarEnvioUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSolicitarEnvioUseCase implements SolicitarEnvioUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingSolicitarEnvioUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando SolicitarEnvio";
	private static final String MENSAGEM_SUCESSO = "SolicitarEnvio concluído";
	private static final String MENSAGEM_FALHA = "SolicitarEnvio falhou: {}";

	private final SolicitarEnvioUseCase delegate;

	public LoggingSolicitarEnvioUseCase(SolicitarEnvioUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public void executar(SolicitarEnvioInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
