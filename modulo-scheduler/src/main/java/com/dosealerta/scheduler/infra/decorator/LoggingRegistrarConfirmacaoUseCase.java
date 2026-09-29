package com.dosealerta.scheduler.infra.decorator;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.usecase.RegistrarConfirmacaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRegistrarConfirmacaoUseCase implements RegistrarConfirmacaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingRegistrarConfirmacaoUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando RegistrarConfirmacao";
	private static final String MENSAGEM_SUCESSO = "RegistrarConfirmacao concluído";
	private static final String MENSAGEM_FALHA = "RegistrarConfirmacao falhou: {}";

	private final RegistrarConfirmacaoUseCase delegate;

	public LoggingRegistrarConfirmacaoUseCase(RegistrarConfirmacaoUseCase delegate) {
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
