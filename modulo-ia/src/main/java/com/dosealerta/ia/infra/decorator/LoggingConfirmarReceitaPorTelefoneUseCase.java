package com.dosealerta.ia.infra.decorator;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingConfirmarReceitaPorTelefoneUseCase implements ConfirmarReceitaPorTelefoneUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingConfirmarReceitaPorTelefoneUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ConfirmarReceitaPorTelefone";
	private static final String MENSAGEM_SUCESSO = "ConfirmarReceitaPorTelefone concluído";
	private static final String MENSAGEM_FALHA = "ConfirmarReceitaPorTelefone falhou: {}";

	private final ConfirmarReceitaPorTelefoneUseCase delegate;

	public LoggingConfirmarReceitaPorTelefoneUseCase(ConfirmarReceitaPorTelefoneUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Receita executar(String telefone, ConfirmarReceitaInput correcoes) {
		log.info(MENSAGEM_INICIO);
		try {
			Receita resultado = delegate.executar(telefone, correcoes);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
