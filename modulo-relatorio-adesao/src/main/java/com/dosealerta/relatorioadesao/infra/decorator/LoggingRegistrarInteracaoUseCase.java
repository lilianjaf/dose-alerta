package com.dosealerta.relatorioadesao.infra.decorator;

import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.usecase.RegistrarInteracaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRegistrarInteracaoUseCase implements RegistrarInteracaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingRegistrarInteracaoUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando RegistrarInteracao";
	private static final String MENSAGEM_SUCESSO = "RegistrarInteracao concluído";
	private static final String MENSAGEM_FALHA = "RegistrarInteracao falhou: {}";

	private final RegistrarInteracaoUseCase delegate;

	public LoggingRegistrarInteracaoUseCase(RegistrarInteracaoUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public void executar(RegistrarInteracaoInput input) {
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
