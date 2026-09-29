package com.dosealerta.usuario.infra.decorator;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.usecase.CompletarCadastroUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingCompletarCadastroUseCase implements CompletarCadastroUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingCompletarCadastroUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando CompletarCadastro";
	private static final String MENSAGEM_SUCESSO = "CompletarCadastro concluído";
	private static final String MENSAGEM_FALHA = "CompletarCadastro falhou: {}";

	private final CompletarCadastroUseCase delegate;

	public LoggingCompletarCadastroUseCase(CompletarCadastroUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Paciente executar(CompletarCadastroInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			Paciente resultado = delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
