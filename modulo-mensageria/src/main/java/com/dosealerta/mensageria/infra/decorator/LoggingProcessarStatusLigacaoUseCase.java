package com.dosealerta.mensageria.infra.decorator;

import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProcessarStatusLigacaoUseCase implements ProcessarStatusLigacaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingProcessarStatusLigacaoUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ProcessarStatusLigacao";
	private static final String MENSAGEM_SUCESSO = "ProcessarStatusLigacao concluído";
	private static final String MENSAGEM_FALHA = "ProcessarStatusLigacao falhou: {}";

	private final ProcessarStatusLigacaoUseCase delegate;

	public LoggingProcessarStatusLigacaoUseCase(ProcessarStatusLigacaoUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public void executar(String telefone, String callStatus) {
		log.info(MENSAGEM_INICIO);
		try {
			delegate.executar(telefone, callStatus);
			log.info(MENSAGEM_SUCESSO);
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
