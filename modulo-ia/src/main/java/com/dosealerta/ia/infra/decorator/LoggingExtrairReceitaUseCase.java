package com.dosealerta.ia.infra.decorator;

import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ResultadoExtracao;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExtrairReceitaUseCase implements ExtrairReceitaUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingExtrairReceitaUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ExtrairReceita";
	private static final String MENSAGEM_SUCESSO = "ExtrairReceita concluído";
	private static final String MENSAGEM_FALHA = "ExtrairReceita falhou: {}";

	private final ExtrairReceitaUseCase delegate;

	public LoggingExtrairReceitaUseCase(ExtrairReceitaUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResultadoExtracao executar(ExtrairReceitaInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			ResultadoExtracao resultado = delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
