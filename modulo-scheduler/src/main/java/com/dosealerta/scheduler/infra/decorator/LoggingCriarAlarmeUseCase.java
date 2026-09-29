package com.dosealerta.scheduler.infra.decorator;

import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingCriarAlarmeUseCase implements CriarAlarmeUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingCriarAlarmeUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando CriarAlarme";
	private static final String MENSAGEM_SUCESSO = "CriarAlarme concluído";
	private static final String MENSAGEM_FALHA = "CriarAlarme falhou: {}";

	private final CriarAlarmeUseCase delegate;

	public LoggingCriarAlarmeUseCase(CriarAlarmeUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResultadoCriarAlarme executar(CriarAlarmeInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			ResultadoCriarAlarme resultado = delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
