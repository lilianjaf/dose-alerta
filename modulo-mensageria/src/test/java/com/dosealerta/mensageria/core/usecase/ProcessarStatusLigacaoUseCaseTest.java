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
class ProcessarStatusLigacaoUseCaseTest {

	@Mock
	private AlarmeClientGateway alarmeClientGateway;

	private ProcessarStatusLigacaoUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarStatusLigacaoUseCase(alarmeClientGateway);
	}

	@Test
	void deveRegistrarLigacaoAtendidaQuandoStatusEmAndamento() {
		useCase.executar("+5511999999999", "in-progress");

		verify(alarmeClientGateway).registrarLigacaoAtendida("+5511999999999");
	}

	@Test
	void naoDeveRegistrarNadaParaOutrosStatus() {
		useCase.executar("+5511999999999", "completed");
		useCase.executar("+5511999999999", "no-answer");
		useCase.executar("+5511999999999", "busy");

		verify(alarmeClientGateway, never()).registrarLigacaoAtendida(anyString());
	}

	@Test
	void naoDeveRegistrarNadaQuandoTelefoneAusente() {
		useCase.executar(null, "in-progress");
		useCase.executar("", "in-progress");
		useCase.executar("  ", "in-progress");

		verify(alarmeClientGateway, never()).registrarLigacaoAtendida(anyString());
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarLigacaoAtendida(anyString());

		useCase.executar("+5511999999999", "in-progress");
	}
}
