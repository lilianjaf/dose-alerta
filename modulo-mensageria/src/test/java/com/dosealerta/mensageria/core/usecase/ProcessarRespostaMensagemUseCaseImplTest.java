package com.dosealerta.mensageria.core.usecase;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.rules.respostamensagem.RespostaMensagemTelefoneDevePreenchidoRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ProcessarRespostaMensagemUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private AlarmeClientGateway alarmeClientGateway;

	@Mock
	private LogGateway logGateway;

	private ProcessarRespostaMensagemUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarRespostaMensagemUseCaseImpl(
				alarmeClientGateway, logGateway, List.of(new RespostaMensagemTelefoneDevePreenchidoRule()));
	}

	@Test
	void deveRegistrarConfirmacaoQuandoBotaoDeConfirmacaoRecebido() {
		boolean confirmou = useCase.executar(TELEFONE, null, "CONFIRMAR");

		verify(alarmeClientGateway).registrarConfirmacao(TELEFONE);
		assertEquals(true, confirmou);
	}

	@Test
	void deveRegistrarConfirmacaoQuandoTextoDigitadoForConfirmar() {
		boolean confirmou = useCase.executar(TELEFONE, "confirmar", null);

		verify(alarmeClientGateway).registrarConfirmacao(TELEFONE);
		assertEquals(true, confirmou);
	}

	@Test
	void naoDeveRegistrarNadaQuandoRespostaNaoForConfirmacao() {
		boolean confirmou = useCase.executar(TELEFONE, "oi, tomei sim", null);

		verify(alarmeClientGateway, never()).registrarConfirmacao(anyString());
		assertEquals(false, confirmou);
	}

	@Test
	void naoDeveLancarExcecaoQuandoModuloSchedulerFalha() {
		doThrow(new RuntimeException("indisponível")).when(alarmeClientGateway).registrarConfirmacao(anyString());

		boolean confirmou = useCase.executar(TELEFONE, null, "CONFIRMAR");

		assertEquals(false, confirmou);
	}
}
