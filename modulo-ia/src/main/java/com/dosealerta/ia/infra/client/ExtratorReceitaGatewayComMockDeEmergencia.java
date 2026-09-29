package com.dosealerta.ia.infra.client;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtratorReceitaGatewayComMockDeEmergencia implements ExtratorReceitaGateway {

	private static final Logger LOG = LoggerFactory.getLogger(ExtratorReceitaGatewayComMockDeEmergencia.class);

	private final ExtratorReceitaGateway delegate;
	private final ExtratorReceitaGateway mock;

	public ExtratorReceitaGatewayComMockDeEmergencia(ExtratorReceitaGateway delegate) {
		this.delegate = delegate;
		this.mock = new MockExtratorReceitaGateway();
	}

	@Override
	public ReceitaExtraida extrair(byte[] imagem) {
		try {
			return delegate.extrair(imagem);
		} catch (ExtracaoReceitaFalhouException e) {

			LOG.warn("Modelo de visão falhou; usando dados fixos para não travar a demonstração", e);
			return mock.extrair(imagem);
		}
	}
}
