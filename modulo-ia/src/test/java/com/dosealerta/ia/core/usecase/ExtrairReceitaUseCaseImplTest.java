package com.dosealerta.ia.core.usecase;

import static com.dosealerta.ia.IaFixtures.CLOCK_FIXO;
import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.PACIENTE_ID;
import static com.dosealerta.ia.IaFixtures.umaExtracaoValida;
import static com.dosealerta.ia.ReceitaExtraidaFixtures.comMedicamentos;
import static com.dosealerta.ia.ReceitaExtraidaFixtures.umMedicamento;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.ReceitaExtraidaFixtures;
import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.dto.ResultadoExtracao;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.TransactionGateway;
import com.dosealerta.ia.core.rules.extrair.ValidadorExtracaoReceitaRule;
import com.dosealerta.ia.core.rules.receitaextraida.ValidadorReceitaExtraidaRule;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ExtrairReceitaUseCaseImplTest extends TesteUnitarioBase {

	private static final MedicamentoExtraido AMOXICILINA = new MedicamentoExtraido("Amoxicilina", "1 comprimido", 8, 7);
	private static final MedicamentoExtraido CELEBRA = new MedicamentoExtraido("Celebra 200mg", "1 cápsula", 12, 5);
	private static final MedicamentoExtraido DECADRON = new MedicamentoExtraido("Decadron 4mg", "2 comprimidos", null, null);
	private static final MedicamentoExtraido TRIANCIL = new MedicamentoExtraido("Triancil", "uso ambulatorial", 0, null);
	private static final MedicamentoExtraido SEM_NOME = new MedicamentoExtraido("  ", "1 comprimido", 8, 7);

	@Mock
	private ExtratorReceitaGateway extratorReceitaGateway;

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private TransactionGateway transactionGateway;

	@Mock
	private ValidadorExtracaoReceitaRule regraDeEntrada;

	@Mock
	private ValidadorReceitaExtraidaRule regraDaReceitaExtraida;

	private ExtrairReceitaUseCaseImpl useCase;
	private ExtrairReceitaInput input;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		useCase = new ExtrairReceitaUseCaseImpl(
				extratorReceitaGateway,
				receitaRepositoryGateway,
				transactionGateway,
				CLOCK_FIXO,
				List.of(regraDeEntrada),
				List.of(regraDaReceitaExtraida));
		input = umaExtracaoValida();
		when(transactionGateway.execute(any(Supplier.class))).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
		when(receitaRepositoryGateway.salvarTodas(anyList())).thenAnswer(inv -> inv.getArgument(0));
	}

	private void extracaoRetorna(ReceitaExtraida extraida) {
		when(extratorReceitaGateway.extrair(input.imagem())).thenReturn(extraida);
	}

	@Test
	void devePersistirAReceitaAguardandoConfirmacaoComDataDoRelogio() {
		extracaoRetorna(umMedicamento("Losartana", "50mg", 24, 30));

		ResultadoExtracao resultado = useCase.executar(input);

		Receita receita = resultado.receitas().get(0);
		assertEquals(1, resultado.receitas().size());
		assertEquals("Losartana", receita.getMedicamento());
		assertEquals(StatusReceita.AGUARDANDO_CONFIRMACAO, receita.getStatus());
		assertEquals(INSTANTE_FIXO, receita.getCriadoEm());
		assertTrue(resultado.naoProcessados().isEmpty());
	}

	@Test
	void deveCriarUmaReceitaParaCadaMedicamentoDaReceita() {
		extracaoRetorna(comMedicamentos(AMOXICILINA, CELEBRA, DECADRON));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(
				List.of("Amoxicilina", "Celebra 200mg", "Decadron 4mg"),
				resultado.receitas().stream().map(Receita::getMedicamento).toList());
		assertEquals(3, resultado.receitas().stream().map(Receita::getId).distinct().count());
		assertTrue(resultado.receitas().stream().allMatch(r -> r.getPacienteId().equals(PACIENTE_ID)));
	}

	@Test
	void naoDeveDescartarMedicamentoSemDoseOuFrequencia() {
		extracaoRetorna(comMedicamentos(AMOXICILINA, DECADRON, TRIANCIL));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(3, resultado.receitas().size());
		assertTrue(resultado.naoProcessados().isEmpty());
		Receita decadron = resultado.receitas().get(1);
		assertNull(decadron.getFrequenciaHoras());
		assertEquals(List.of("frequenciaHoras", "duracaoDias"), decadron.camposPendentes());
		Receita triancil = resultado.receitas().get(2);
		assertNull(triancil.getDose());
		assertEquals(List.of("dose", "frequenciaHoras", "duracaoDias"), triancil.camposPendentes());
	}

	@Test
	void deveListarComoNaoProcessadoSomenteOItemSemNome() {
		extracaoRetorna(comMedicamentos(AMOXICILINA, SEM_NOME));

		ResultadoExtracao resultado = useCase.executar(input);

		assertEquals(1, resultado.receitas().size());
		assertEquals(1, resultado.naoProcessados().size());
		assertEquals("medicamento não identificado", resultado.naoProcessados().get(0).motivo());
	}

	@Test
	void naoDevePersistirQuandoNenhumMedicamentoTemNome() {
		extracaoRetorna(umMedicamento(null, "50mg", 24, 30));

		assertThrows(ReceitaInvalidaException.class, () -> useCase.executar(input));

		verify(receitaRepositoryGateway, never()).salvarTodas(anyList());
	}

	@Test
	void naoDeveChamarOExtratorQuandoAlgumaRegraDeEntradaFalha() {
		doThrow(new ImagemReceitaInvalidaException("x")).when(regraDeEntrada).validar(any());

		assertThrows(ImagemReceitaInvalidaException.class, () -> useCase.executar(input));

		verifyNoInteractions(extratorReceitaGateway, receitaRepositoryGateway);
	}

	@Test
	void naoDevePersistirQuandoAlgumaRegraDaReceitaExtraidaFalha() {
		extracaoRetorna(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30));
		doThrow(new ReceitaFormalNaoIdentificadaException("motivo")).when(regraDaReceitaExtraida).validar(any());

		assertThrows(ReceitaFormalNaoIdentificadaException.class, () -> useCase.executar(input));

		verifyNoInteractions(receitaRepositoryGateway);
	}
}
