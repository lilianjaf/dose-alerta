package com.dosealerta.ia.core.usecase;

import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.rules.confirmarportelefone.ValidadorConfirmacaoPorTelefoneRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ConfirmarReceitaPorTelefoneUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private ConfirmarReceitaUseCase confirmarReceitaUseCase;

	@Mock
	private ValidadorConfirmacaoPorTelefoneRule regra;

	private ConfirmarReceitaPorTelefoneUseCaseImpl useCase;
	private Receita pendente;
	private ConfirmarReceitaInput correcoes;

	@BeforeEach
	void setUp() {
		useCase = new ConfirmarReceitaPorTelefoneUseCaseImpl(
				receitaRepositoryGateway, confirmarReceitaUseCase, List.of(regra));
		pendente = umaReceita();
		correcoes = ConfirmarReceitaInput.semCorrecoes();
	}

	@Test
	void deveConfirmarAReceitaPendenteDoTelefone() {
		when(receitaRepositoryGateway.buscarAguardandoConfirmacaoMaisRecentePorTelefone(TELEFONE))
				.thenReturn(Optional.of(pendente));
		when(confirmarReceitaUseCase.executar(pendente.getId(), correcoes)).thenReturn(pendente);

		assertSame(pendente, useCase.executar(TELEFONE, correcoes));
	}

	@Test
	void naoDeveConfirmarQuandoAlgumaRegraFalha() {
		when(receitaRepositoryGateway.buscarAguardandoConfirmacaoMaisRecentePorTelefone(TELEFONE))
				.thenReturn(Optional.empty());
		doThrow(new ReceitaNaoEncontradaException(TELEFONE)).when(regra).validar(any());

		assertThrows(ReceitaNaoEncontradaException.class, () -> useCase.executar(TELEFONE, correcoes));

		verify(confirmarReceitaUseCase, never()).executar(any(), any());
	}
}
