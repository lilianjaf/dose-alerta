package com.dosealerta.ia.infra.client;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rede de segurança para demonstração: se todos os modelos configurados falharem (Gemini fora do ar ou
 * sobrecarregado — ver {@link GeminiExtratorReceitaGateway}), devolve dados fixos em vez de propagar o erro pro
 * paciente no WhatsApp. Só entra em ação se {@code ia.mock-de-emergencia-habilitado=true} (ver
 * {@code UseCaseConfig}); desligado por padrão, porque mascara uma falha real do modelo de visão.
 */
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
			// Só cobre falha do modelo (Gemini fora do ar, sobrecarregado, resposta inválida). Imagem inválida
			// (ImagemReceitaInvalidaException) continua propagando: não é um problema de disponibilidade da IA.
			LOG.warn("Modelo de visão falhou; usando dados fixos para não travar a demonstração", e);
			return mock.extrair(imagem);
		}
	}
}
