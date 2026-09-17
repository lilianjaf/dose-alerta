package com.dosealerta.mensageria.core.usecase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessarConfirmacaoLigacaoUseCaseTest {

	@Mock
	private AlarmeClientGateway alarmeClientGateway;

	private ProcessarConfirmacaoLigacaoUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarConfirmacaoLigacaoUseCase(alarmeClientGateway);
	}

	@Test
	void deveConfirmarERegistrarQuandoDigitoCorreto() {
		boolean confirmado = useCase.executar("+5511999999999", "1");

		assertTrue(confirmado);
		verify(alarmeClientGateway).registrarConfirmacao("+5511999999999");
	}

	@Test
	void naoDeveRegistrarQuandoDigitoIncorreto() {
		boolean confirmado = useCase.executar("+5511999999999", "9");

		assertFalse(confirmado);
		verify(alarmeClientGateway, never()).registrarConfirmacao(anyString());
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarConfirmacao(anyString());

		boolean confirmado = useCase.executar("+5511999999999", "1");

		assertTrue(confirmado);
	}
}
