package com.dosealerta.relatorioadesao.infra.decorator;

import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.usecase.ConsultarTaxaAdesaoUseCase;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingConsultarTaxaAdesaoUseCase implements ConsultarTaxaAdesaoUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingConsultarTaxaAdesaoUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando ConsultarTaxaAdesao";
	private static final String MENSAGEM_SUCESSO = "ConsultarTaxaAdesao concluído";
	private static final String MENSAGEM_FALHA = "ConsultarTaxaAdesao falhou: {}";

	private final ConsultarTaxaAdesaoUseCase delegate;

	public LoggingConsultarTaxaAdesaoUseCase(ConsultarTaxaAdesaoUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<TaxaAdesao> executar(UUID pacienteId, Instant inicio, Instant fim) {
		log.info(MENSAGEM_INICIO);
		try {
			List<TaxaAdesao> resultado = delegate.executar(pacienteId, inicio, fim);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
