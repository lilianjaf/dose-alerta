package com.dosealerta.mensageria.core.usecase;

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
		useCase.executar("+5511999999999", null, "CONFIRMAR");

		verify(alarmeClientGateway).registrarConfirmacao("+5511999999999");
	}

	@Test
	void deveRegistrarConfirmacaoQuandoTextoDigitadoForConfirmar() {
		useCase.executar("+5511999999999", "confirmar", null);

		verify(alarmeClientGateway).registrarConfirmacao("+5511999999999");
	}

	@Test
	void naoDeveRegistrarNadaQuandoRespostaNaoForConfirmacao() {
		useCase.executar("+5511999999999", "oi, tomei sim", null);

		verify(alarmeClientGateway, never()).registrarConfirmacao(anyString());
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarConfirmacao(anyString());

		useCase.executar("+5511999999999", null, "CONFIRMAR");
	}
}
