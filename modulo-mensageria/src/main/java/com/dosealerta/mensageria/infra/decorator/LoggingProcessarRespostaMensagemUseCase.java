package com.dosealerta.mensageria.infra.decorator;

import com.dosealerta.mensageria.core.usecase.ProcessarRespostaMensagemUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProcessarRespostaMensagemUseCase implements ProcessarRespostaMensagemUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingProcessarRespostaMensagemUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ProcessarRespostaMensagem";
	private static final String MENSAGEM_SUCESSO = "ProcessarRespostaMensagem concluído";
	private static final String MENSAGEM_FALHA = "ProcessarRespostaMensagem falhou: {}";

	private final ProcessarRespostaMensagemUseCase delegate;

	public LoggingProcessarRespostaMensagemUseCase(ProcessarRespostaMensagemUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean executar(String telefone, String corpo, String textoBotao) {
		log.info(MENSAGEM_INICIO);
		try {
			boolean resultado = delegate.executar(telefone, corpo, textoBotao);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
