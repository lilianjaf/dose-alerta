package com.dosealerta.scheduler.infra.decorator;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCase;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingBuscarAlarmeUseCase implements BuscarAlarmeUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingBuscarAlarmeUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando BuscarAlarme";
	private static final String MENSAGEM_SUCESSO = "BuscarAlarme concluído";
	private static final String MENSAGEM_FALHA = "BuscarAlarme falhou: {}";

	private final BuscarAlarmeUseCase delegate;

	public LoggingBuscarAlarmeUseCase(BuscarAlarmeUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Alarme executar(UUID id) {
		log.info(MENSAGEM_INICIO);
		try {
			Alarme resultado = delegate.executar(id);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
