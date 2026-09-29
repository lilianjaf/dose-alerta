package com.dosealerta.ia.core.usecase;

import static com.dosealerta.ia.IaFixtures.CLOCK_FIXO;
import static com.dosealerta.ia.IaFixtures.CORRELATION_ID;
import static com.dosealerta.ia.IaFixtures.DOSE;
import static com.dosealerta.ia.IaFixtures.MEDICAMENTO;
import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.umaConfirmacao;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static com.dosealerta.ia.IaFixtures.umaReceitaCom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.domain.FeedbackExtracao;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.CorrelacaoGateway;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.TransactionGateway;
import com.dosealerta.ia.core.rules.confirmar.ConfirmacaoReceitaContext;
import com.dosealerta.ia.core.rules.confirmar.ValidadorConfirmacaoReceitaRule;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class ConfirmarReceitaUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;

	@Mock
	private TransactionGateway transactionGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	@Mock
	private ValidadorConfirmacaoReceitaRule regra;

	@Captor
	private ArgumentCaptor<FeedbackExtracao> feedbackCaptor;

	@Captor
	private ArgumentCaptor<ConfirmacaoReceitaContext> contextCaptor;

	private ConfirmarReceitaUseCaseImpl useCase;
	private Receita receita;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		useCase = new ConfirmarReceitaUseCaseImpl(
				receitaRepositoryGateway,
				feedbackExtracaoRepositoryGateway,
				transactionGateway,
				correlacaoGateway,
				CLOCK_FIXO,
				List.of(regra));
		receita = umaReceita();
		when(transactionGateway.execute(any(Supplier.class))).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
		when(correlacaoGateway.atual()).thenReturn(CORRELATION_ID);
		when(receitaRepositoryGateway.buscarPorId(RECEITA_ID)).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void deveConfirmarSemMarcarCorrecaoQuandoValoresSaoIguaisAosExtraidos() {
		Receita resultado = useCase.executar(RECEITA_ID, umaConfirmacao(MEDICAMENTO, DOSE, 24, 30));

		assertEquals(StatusReceita.CONFIRMADA, resultado.getStatus());
		verify(feedbackExtracaoRepositoryGateway).salvar(feedbackCaptor.capture());
		assertFalse(feedbackCaptor.getValue().corrigido());
	}

	@Test
	void deveMarcarCorrecaoQuandoPacienteAlteraAlgumCampo() {
		Receita resultado = useCase.executar(RECEITA_ID, umaConfirmacao(MEDICAMENTO, "100mg", 24, 30));

		assertEquals("100mg", resultado.getDose());
		verify(feedbackExtracaoRepositoryGateway).salvar(feedbackCaptor.capture());
		assertTrue(feedbackCaptor.getValue().corrigido());
		assertEquals(DOSE, feedbackCaptor.getValue().doseExtraida());
		assertEquals("100mg", feedbackCaptor.getValue().doseConfirmada());
	}

	@Test
	void deveManterOsDadosExtraidosQuandoNenhumCampoEEnviado() {
		Receita resultado = useCase.executar(RECEITA_ID, ConfirmarReceitaInput.semCorrecoes());

		assertEquals(StatusReceita.CONFIRMADA, resultado.getStatus());
		assertEquals(MEDICAMENTO, resultado.getMedicamento());
		assertEquals(DOSE, resultado.getDose());
		verify(feedbackExtracaoRepositoryGateway).salvar(feedbackCaptor.capture());
		assertFalse(feedbackCaptor.getValue().corrigido());
	}

	@Test
	void deveCorrigirSomenteOsCamposEnviadosEManterOsDemais() {
		Receita resultado = useCase.executar(RECEITA_ID, umaConfirmacao(null, "100mg", null, 60));

		assertEquals(MEDICAMENTO, resultado.getMedicamento());
		assertEquals("100mg", resultado.getDose());
		assertEquals(24, resultado.getFrequenciaHoras());
		assertEquals(60, resultado.getDuracaoDias());
	}

	@Test
	void deveCompletarOsCamposQueAReceitaNaoTrouxeComOQueOPacienteInformou() {
		receita = umaReceitaCom("Amoxicilina", "1 comprimido", 8, null);
		when(receitaRepositoryGateway.buscarPorId(RECEITA_ID)).thenReturn(Optional.of(receita));

		Receita resultado = useCase.executar(RECEITA_ID, umaConfirmacao(null, null, null, 7));

		assertEquals(7, resultado.getDuracaoDias());
		verify(feedbackExtracaoRepositoryGateway).salvar(feedbackCaptor.capture());
		assertTrue(feedbackCaptor.getValue().corrigido());
		assertNull(feedbackCaptor.getValue().duracaoExtraidaDias());
	}

	@Test
	void deveGerarOEventoDeOutboxComACorrelacaoAtual() {
		Receita resultado = useCase.executar(RECEITA_ID, ConfirmarReceitaInput.semCorrecoes());

		assertEquals(CORRELATION_ID, resultado.getEventosOutbox().get(0).correlationId());
	}

	@Test
	void devePassarPeloContextComOsValoresMesclados() {
		useCase.executar(RECEITA_ID, umaConfirmacao(null, "100mg", null, null));

		verify(regra).validar(contextCaptor.capture());
		assertEquals("100mg", contextCaptor.getValue().dose());
		assertEquals(24, contextCaptor.getValue().frequenciaHoras());
	}

	@Test
	void naoDeveSalvarNadaQuandoAlgumaRegraFalha() {
		doThrow(new ReceitaNaoEncontradaException(RECEITA_ID)).when(regra).validar(any());

		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> useCase.executar(RECEITA_ID, ConfirmarReceitaInput.semCorrecoes()));

		verifyNoInteractions(feedbackExtracaoRepositoryGateway, transactionGateway);
	}

	@Test
	void devePassarReceitaNulaAsRegrasQuandoNaoExiste() {
		when(receitaRepositoryGateway.buscarPorId(RECEITA_ID)).thenReturn(Optional.empty());
		doThrow(new ReceitaNaoEncontradaException(RECEITA_ID)).when(regra).validar(any());

		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> useCase.executar(RECEITA_ID, ConfirmarReceitaInput.semCorrecoes()));

		verify(regra).validar(contextCaptor.capture());
		assertNull(contextCaptor.getValue().receita());
	}
}
