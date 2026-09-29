package com.dosealerta.ia.infra.decorator;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingConfirmarReceitaUseCase implements ConfirmarReceitaUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingConfirmarReceitaUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ConfirmarReceita";
	private static final String MENSAGEM_SUCESSO = "ConfirmarReceita concluído";
	private static final String MENSAGEM_FALHA = "ConfirmarReceita falhou: {}";

	private final ConfirmarReceitaUseCase delegate;

	public LoggingConfirmarReceitaUseCase(ConfirmarReceitaUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Receita executar(UUID receitaId, ConfirmarReceitaInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			Receita resultado = delegate.executar(receitaId, input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
