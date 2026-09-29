package com.dosealerta.ia.infra.decorator;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingBuscarReceitaUseCase implements BuscarReceitaUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingBuscarReceitaUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando BuscarReceita";
	private static final String MENSAGEM_SUCESSO = "BuscarReceita concluído";
	private static final String MENSAGEM_FALHA = "BuscarReceita falhou: {}";

	private final BuscarReceitaUseCase delegate;

	public LoggingBuscarReceitaUseCase(BuscarReceitaUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Receita executar(UUID id) {
		log.info(MENSAGEM_INICIO);
		try {
			Receita resultado = delegate.executar(id);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
