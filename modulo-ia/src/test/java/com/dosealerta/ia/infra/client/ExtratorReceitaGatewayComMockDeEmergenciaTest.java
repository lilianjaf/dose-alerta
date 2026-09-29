package com.dosealerta.ia.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ExtratorReceitaGatewayComMockDeEmergenciaTest extends TesteUnitarioBase {

	private static final byte[] IMAGEM = new byte[] {1, 2, 3};

	@Mock
	private ExtratorReceitaGateway delegate;

	private ExtratorReceitaGatewayComMockDeEmergencia gateway;

	@BeforeEach
	void setUp() {
		gateway = new ExtratorReceitaGatewayComMockDeEmergencia(delegate);
	}

	@Test
	void deveDevolverOResultadoDoDelegateQuandoElaFunciona() {
		ReceitaExtraida esperada = new ReceitaExtraida(true, "Dr. Real", "CRM 1", List.of());
		when(delegate.extrair(IMAGEM)).thenReturn(esperada);

		assertEquals(esperada, gateway.extrair(IMAGEM));
	}

	@Test
	void deveCairParaOMockQuandoOModeloDeVisaoFalha() {
		when(delegate.extrair(IMAGEM)).thenThrow(new ExtracaoReceitaFalhouException("Falha ao chamar o modelo de visão"));

		ReceitaExtraida resultado = gateway.extrair(IMAGEM);

		assertTrue(resultado.receitaMedica());
		assertEquals(2, resultado.medicamentos().size());
	}

	@Test
	void naoDeveMascararUmaImagemInvalida() {
		when(delegate.extrair(IMAGEM)).thenThrow(new ImagemReceitaInvalidaException("Formato não suportado"));

		assertThrows(ImagemReceitaInvalidaException.class, () -> gateway.extrair(IMAGEM));
	}
}
