package com.dosealerta.ia.infra.decorator;

import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static com.dosealerta.ia.IaFixtures.umaExtracaoValida;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ResultadoExtracao;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import com.dosealerta.ia.core.usecase.PublicarEventosPendentesUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class LoggingUseCasesTest extends TesteUnitarioBase {

	@Mock
	private BuscarReceitaUseCase buscar;

	@Mock
	private ConfirmarReceitaUseCase confirmar;

	@Mock
	private ConfirmarReceitaPorTelefoneUseCase confirmarPorTelefone;

	@Mock
	private ExtrairReceitaUseCase extrair;

	@Mock
	private PublicarEventosPendentesUseCase publicar;

	private Receita receita;
	private ConfirmarReceitaInput correcoes;
	private ExtrairReceitaInput extracao;

	@BeforeEach
	void setUp() {
		receita = umaReceita();
		correcoes = ConfirmarReceitaInput.semCorrecoes();
		extracao = umaExtracaoValida();
	}

	@Test
	void deveDelegarEBuscar() {
		when(buscar.executar(RECEITA_ID)).thenReturn(receita);

		assertSame(receita, new LoggingBuscarReceitaUseCase(buscar).executar(RECEITA_ID));
	}

	@Test
	void deveDelegarEConfirmar() {
		when(confirmar.executar(RECEITA_ID, correcoes)).thenReturn(receita);

		assertSame(receita, new LoggingConfirmarReceitaUseCase(confirmar).executar(RECEITA_ID, correcoes));
	}

	@Test
	void deveDelegarEConfirmarPorTelefone() {
		when(confirmarPorTelefone.executar(TELEFONE, correcoes)).thenReturn(receita);

		assertSame(receita, new LoggingConfirmarReceitaPorTelefoneUseCase(confirmarPorTelefone).executar(TELEFONE, correcoes));
	}

	@Test
	void deveDelegarEExtrair() {
		var resultado = new ResultadoExtracao(List.of(receita), List.of());
		when(extrair.executar(extracao)).thenReturn(resultado);

		assertSame(resultado, new LoggingExtrairReceitaUseCase(extrair).executar(extracao));
	}

	@Test
	void deveDelegarEPublicar() {
		new LoggingPublicarEventosPendentesUseCase(publicar).executar();

		verify(publicar).executar();
	}

	@Test
	void devePropagarAExcecaoDoDelegate() {
		when(buscar.executar(RECEITA_ID)).thenThrow(new ReceitaNaoEncontradaException(RECEITA_ID));

		assertThrows(
				ReceitaNaoEncontradaException.class, () -> new LoggingBuscarReceitaUseCase(buscar).executar(RECEITA_ID));
	}
}
