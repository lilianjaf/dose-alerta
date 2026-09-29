package com.dosealerta.mensageria.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class ProcessarRespostaMensagemUseCaseTest {

	@Mock
	private AlarmeClientGateway alarmeClientGateway;

	private ProcessarRespostaMensagemUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarRespostaMensagemUseCase(alarmeClientGateway);
	}

	@Test
	void deveRegistrarConfirmacaoQuandoBotaoDeConfirmacaoRecebido() {
		boolean confirmou = useCase.executar("+5511999999999", null, "CONFIRMAR");

		verify(alarmeClientGateway).registrarConfirmacao("+5511999999999");
		assertEquals(true, confirmou);
	}

	@Test
	void deveRegistrarConfirmacaoQuandoTextoDigitadoForConfirmar() {
		boolean confirmou = useCase.executar("+5511999999999", "confirmar", null);

		verify(alarmeClientGateway).registrarConfirmacao("+5511999999999");
		assertEquals(true, confirmou);
	}

	@Test
	void naoDeveRegistrarNadaQuandoRespostaNaoForConfirmacao() {
		boolean confirmou = useCase.executar("+5511999999999", "oi, tomei sim", null);

		verify(alarmeClientGateway, never()).registrarConfirmacao(anyString());
		assertEquals(false, confirmou);
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarConfirmacao(anyString());

		boolean confirmou = useCase.executar("+5511999999999", null, "CONFIRMAR");

		assertEquals(false, confirmou);
	}
}
