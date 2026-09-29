package com.dosealerta.mensageria.core.usecase;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.rules.confirmacaoligacao.ConfirmacaoLigacaoTelefoneDevePreenchidoRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ProcessarConfirmacaoLigacaoUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private AlarmeClientGateway alarmeClientGateway;

	@Mock
	private LogGateway logGateway;

	private ProcessarConfirmacaoLigacaoUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarConfirmacaoLigacaoUseCaseImpl(
				alarmeClientGateway, logGateway, List.of(new ConfirmacaoLigacaoTelefoneDevePreenchidoRule()));
	}

	@Test
	void deveConfirmarERegistrarQuandoDigitoCorreto() {
		boolean confirmado = useCase.executar(TELEFONE, "1");

		assertTrue(confirmado);
		verify(alarmeClientGateway).registrarConfirmacao(TELEFONE);
	}

	@Test
	void naoDeveRegistrarQuandoDigitoIncorreto() {
		boolean confirmado = useCase.executar(TELEFONE, "9");

		assertFalse(confirmado);
		verify(alarmeClientGateway, never()).registrarConfirmacao(anyString());
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarConfirmacao(anyString());

		boolean confirmado = useCase.executar(TELEFONE, "1");

		assertTrue(confirmado);
	}
}
