package com.dosealerta.mensageria.infra.decorator;

import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProcessarConfirmacaoLigacaoUseCase implements ProcessarConfirmacaoLigacaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingProcessarConfirmacaoLigacaoUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ProcessarConfirmacaoLigacao";
	private static final String MENSAGEM_SUCESSO = "ProcessarConfirmacaoLigacao concluído";
	private static final String MENSAGEM_FALHA = "ProcessarConfirmacaoLigacao falhou: {}";

	private final ProcessarConfirmacaoLigacaoUseCase delegate;

	public LoggingProcessarConfirmacaoLigacaoUseCase(ProcessarConfirmacaoLigacaoUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean executar(String telefone, String digitos) {
		log.info(MENSAGEM_INICIO);
		try {
			boolean resultado = delegate.executar(telefone, digitos);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
