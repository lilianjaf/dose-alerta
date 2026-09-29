package com.dosealerta.mensageria.infra.decorator;

import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProcessarMensagemRecebidaUseCase implements ProcessarMensagemRecebidaUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingProcessarMensagemRecebidaUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ProcessarMensagemRecebida";
	private static final String MENSAGEM_SUCESSO = "ProcessarMensagemRecebida concluído";
	private static final String MENSAGEM_FALHA = "ProcessarMensagemRecebida falhou: {}";

	private final ProcessarMensagemRecebidaUseCase delegate;

	public LoggingProcessarMensagemRecebidaUseCase(ProcessarMensagemRecebidaUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public void executar(DadosMensagemRecebida dados) {
		log.info(MENSAGEM_INICIO);
		try {
			delegate.executar(dados);
			log.info(MENSAGEM_SUCESSO);
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
