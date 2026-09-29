package com.dosealerta.scheduler.infra.decorator;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.usecase.RegistrarLigacaoAtendidaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRegistrarLigacaoAtendidaUseCase implements RegistrarLigacaoAtendidaUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingRegistrarLigacaoAtendidaUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando RegistrarLigacaoAtendida";
	private static final String MENSAGEM_SUCESSO = "RegistrarLigacaoAtendida concluído";
	private static final String MENSAGEM_FALHA = "RegistrarLigacaoAtendida falhou: {}";

	private final RegistrarLigacaoAtendidaUseCase delegate;

	public LoggingRegistrarLigacaoAtendidaUseCase(RegistrarLigacaoAtendidaUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Alarme executar(String telefone) {
		log.info(MENSAGEM_INICIO);
		try {
			Alarme resultado = delegate.executar(telefone);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
