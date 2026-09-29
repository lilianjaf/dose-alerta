package com.dosealerta.ia.core.usecase;

import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.rules.buscar.BuscaReceitaContext;
import com.dosealerta.ia.core.rules.buscar.ValidadorBuscaReceitaRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class BuscarReceitaUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private ValidadorBuscaReceitaRule regra;

	@Captor
	private ArgumentCaptor<BuscaReceitaContext> contextCaptor;

	private BuscarReceitaUseCaseImpl useCase;
	private Receita receita;

	@BeforeEach
	void setUp() {
		useCase = new BuscarReceitaUseCaseImpl(receitaRepositoryGateway, List.of(regra));
		receita = umaReceita();
	}

	@Test
	void deveDevolverAReceitaEncontrada() {
		when(receitaRepositoryGateway.buscarPorId(RECEITA_ID)).thenReturn(Optional.of(receita));

		assertSame(receita, useCase.executar(RECEITA_ID));
	}

	@Test
	void devePassarReceitaNulaAsRegrasQuandoNaoExiste() {
		when(receitaRepositoryGateway.buscarPorId(RECEITA_ID)).thenReturn(Optional.empty());
		doThrow(new ReceitaNaoEncontradaException(RECEITA_ID)).when(regra).validar(any());

		assertThrows(ReceitaNaoEncontradaException.class, () -> useCase.executar(RECEITA_ID));

		verify(regra).validar(contextCaptor.capture());
		assertSame(null, contextCaptor.getValue().receita());
	}

	@Test
	void naoDeveConsultarOGatewayQuandoOIdENulo() {
		doThrow(new ReceitaNaoEncontradaException(RECEITA_ID)).when(regra).validar(any());

		assertThrows(ReceitaNaoEncontradaException.class, () -> useCase.executar(null));

		verifyNoInteractions(receitaRepositoryGateway);
	}
}
