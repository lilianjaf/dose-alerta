package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfirmarReceitaPorTelefoneUseCaseTest {

	private static final String TELEFONE = "+5511999999999";

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;

	private ConfirmarReceitaPorTelefoneUseCase useCase;

	@BeforeEach
	void setUp() {
		ConfirmarReceitaUseCase confirmarReceitaUseCase =
				new ConfirmarReceitaUseCase(receitaRepositoryGateway, feedbackExtracaoRepositoryGateway);
		useCase = new ConfirmarReceitaPorTelefoneUseCase(receitaRepositoryGateway, confirmarReceitaUseCase);
	}

	@Test
	void deveConfirmarAReceitaPendenteDoTelefone() {
		Receita pendente = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), TELEFONE, "Losartana", "50mg", 24, 30, Instant.now());
		when(receitaRepositoryGateway.buscarAguardandoConfirmacaoMaisRecentePorTelefone(TELEFONE))
				.thenReturn(Optional.of(pendente));
		when(receitaRepositoryGateway.buscarPorId(pendente.getId())).thenReturn(Optional.of(pendente));
		when(receitaRepositoryGateway.salvar(ArgumentMatchers.any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

		Receita resultado = useCase.executar(TELEFONE, ConfirmarReceitaInput.semCorrecoes());

		assertEquals(StatusReceita.CONFIRMADA, resultado.getStatus());
	}

	@Test
	void deveLancarExcecaoQuandoNaoHaReceitaPendenteParaOTelefone() {
		when(receitaRepositoryGateway.buscarAguardandoConfirmacaoMaisRecentePorTelefone(TELEFONE))
				.thenReturn(Optional.empty());

		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> useCase.executar(TELEFONE, ConfirmarReceitaInput.semCorrecoes()));
	}
}
