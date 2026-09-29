package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.ReceitaExtraidaFixtures;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.dto.ResultadoExtracao;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtrairReceitaUseCaseTest {

	@Mock
	private ExtratorReceitaGateway extratorReceitaGateway;

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	private ExtrairReceitaUseCase useCase;
	private ExtrairReceitaInput input;

	@BeforeEach
	void setUp() {
		useCase = new ExtrairReceitaUseCase(extratorReceitaGateway, receitaRepositoryGateway);
		input = new ExtrairReceitaInput(UUID.randomUUID(), "+5511999999999", Instant.now(), new byte[] {1, 2, 3});
	}

	@Test
	void devePersistirAReceitaAguardandoConfirmacaoQuandoExtracaoEValida() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30));
		when(receitaRepositoryGateway.salvarTodas(anyList())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(1, resultado.receitas().size());
		assertEquals("Losartana", resultado.receitas().get(0).getMedicamento());
		assertEquals(StatusReceita.AGUARDANDO_CONFIRMACAO, resultado.receitas().get(0).getStatus());
		assertTrue(resultado.naoProcessados().isEmpty());
	}

	@Test
	void deveCriarUmaReceitaParaCadaMedicamentoDaReceita() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(ReceitaExtraidaFixtures.comMedicamentos(
						new MedicamentoExtraido("Amoxicilina 500mg", "1 comprimido", 8, 7),
						new MedicamentoExtraido("Celebra 200mg", "1 cápsula", 12, 5),
						new MedicamentoExtraido("Tylex 30mg", "1 comprimido", 4, 3)));
		when(receitaRepositoryGateway.salvarTodas(anyList())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(
				List.of("Amoxicilina 500mg", "Celebra 200mg", "Tylex 30mg"),
				resultado.receitas().stream().map(Receita::getMedicamento).toList());
		assertEquals(3, resultado.receitas().stream().map(Receita::getId).distinct().count());
		assertTrue(resultado.receitas().stream().allMatch(r -> r.getPacienteId().equals(input.pacienteId())));
	}

	@Test
	void devePersistirSemDuracaoQuandoAReceitaNaoInforma() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(ReceitaExtraidaFixtures.umMedicamento("Amoxicilina", "1 comprimido", 8, null));
		when(receitaRepositoryGateway.salvarTodas(anyList())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoExtracao resultado = useCase.executar(input);

		assertNull(resultado.receitas().get(0).getDuracaoDias());
	}

	@Test
	void naoDeveDescartarMedicamentoSemDoseOuFrequencia() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(ReceitaExtraidaFixtures.comMedicamentos(
						new MedicamentoExtraido("Amoxicilina", "1 comprimido", 8, 7),
						new MedicamentoExtraido("Decadron 4mg", "2 comprimidos", null, null),
						new MedicamentoExtraido("Triancil", "uso ambulatorial", 0, null)));
		when(receitaRepositoryGateway.salvarTodas(anyList())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(3, resultado.receitas().size());
		assertTrue(resultado.naoProcessados().isEmpty());
		Receita decadron = resultado.receitas().get(1);
		assertEquals("2 comprimidos", decadron.getDose());
		assertNull(decadron.getFrequenciaHoras());
		assertEquals(List.of("frequenciaHoras", "duracaoDias"), decadron.camposPendentes());

		Receita triancil = resultado.receitas().get(2);
		assertNull(triancil.getDose());
		assertNull(triancil.getFrequenciaHoras());
		assertEquals(List.of("dose", "frequenciaHoras", "duracaoDias"), triancil.camposPendentes());
	}

	@Test
	void deveListarComoNaoProcessadoSomenteOItemSemNome() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(ReceitaExtraidaFixtures.comMedicamentos(
						new MedicamentoExtraido("Amoxicilina", "1 comprimido", 8, 7),
						new MedicamentoExtraido("  ", "1 comprimido", 8, 7)));
		when(receitaRepositoryGateway.salvarTodas(anyList())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(1, resultado.receitas().size());
		assertEquals(1, resultado.naoProcessados().size());
		assertEquals("medicamento não identificado", resultado.naoProcessados().get(0).motivo());
	}

	@Test
	void naoDevePersistirQuandoNenhumMedicamentoTemNome() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(ReceitaExtraidaFixtures.umMedicamento(null, "50mg", 24, 30));

		assertThrows(ReceitaInvalidaException.class, () -> useCase.executar(input));
		verifyNoInteractions(receitaRepositoryGateway);
	}

	@Test
	void naoDevePersistirQuandoAImagemNaoEUmaReceitaFormal() {
		when(extratorReceitaGateway.extrair(input.imagem()))
				.thenReturn(new ReceitaExtraida(
						true, "Dra. Exemplo", null, List.of(new MedicamentoExtraido("Losartana", "50mg", 24, 30))));

		assertThrows(ReceitaFormalNaoIdentificadaException.class, () -> useCase.executar(input));
		verifyNoInteractions(receitaRepositoryGateway);
	}
}
