package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.core.domain.FeedbackExtracao;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfirmarReceitaUseCaseTest {

	@Mock
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Mock
	private FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;

	private ConfirmarReceitaUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ConfirmarReceitaUseCase(receitaRepositoryGateway, feedbackExtracaoRepositoryGateway);
	}

	@Test
	void deveConfirmarSemMarcarCorrecaoQuandoValoresSaoIguaisAosExtraidos() {
		Receita receita = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, Instant.now());
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

		var input = new ConfirmarReceitaInput("Losartana", "50mg", 24, 30);
		Receita resultado = useCase.executar(receita.getId(), input);

		assertEquals(StatusReceita.CONFIRMADA, resultado.getStatus());
		ArgumentCaptor<FeedbackExtracao> captor = ArgumentCaptor.forClass(FeedbackExtracao.class);
		verify(feedbackExtracaoRepositoryGateway).salvar(captor.capture());
		assertFalse(captor.getValue().corrigido());
	}

	@Test
	void deveMarcarCorrecaoQuandoPacienteAlteraAlgumCampo() {
		Receita receita = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, Instant.now());
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

		var input = new ConfirmarReceitaInput("Losartana", "100mg", 24, 30);
		Receita resultado = useCase.executar(receita.getId(), input);

		assertEquals("100mg", resultado.getDose());
		ArgumentCaptor<FeedbackExtracao> captor = ArgumentCaptor.forClass(FeedbackExtracao.class);
		verify(feedbackExtracaoRepositoryGateway).salvar(captor.capture());
		assertTrue(captor.getValue().corrigido());
		assertEquals("50mg", captor.getValue().doseExtraida());
		assertEquals("100mg", captor.getValue().doseConfirmada());
	}

	@Test
	void deveManterOsDadosExtraidosQuandoNenhumCampoEEnviado() {
		Receita receita = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Aerolin spray 100 mcg", "2 doses", 6, 30, Instant.now());
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

		Receita resultado = useCase.executar(receita.getId(), ConfirmarReceitaInput.semCorrecoes());

		assertEquals(StatusReceita.CONFIRMADA, resultado.getStatus());
		assertEquals("Aerolin spray 100 mcg", resultado.getMedicamento());
		assertEquals("2 doses", resultado.getDose());
		assertEquals(6, resultado.getFrequenciaHoras());
		assertEquals(30, resultado.getDuracaoDias());
		ArgumentCaptor<FeedbackExtracao> captor = ArgumentCaptor.forClass(FeedbackExtracao.class);
		verify(feedbackExtracaoRepositoryGateway).salvar(captor.capture());
		assertFalse(captor.getValue().corrigido());
	}

	@Test
	void deveCorrigirSomenteOsCamposEnviadosEManterOsDemais() {
		Receita receita = Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, Instant.now());
		when(receitaRepositoryGateway.buscarPorId(receita.getId())).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class))).thenAnswer(inv -> inv.getArgument(0));

		Receita resultado =
				useCase.executar(receita.getId(), new ConfirmarReceitaInput(null, "100mg", null, 60));

		assertEquals("Losartana", resultado.getMedicamento());
		assertEquals("100mg", resultado.getDose());
		assertEquals(24, resultado.getFrequenciaHoras());
		assertEquals(60, resultado.getDuracaoDias());
		ArgumentCaptor<FeedbackExtracao> captor = ArgumentCaptor.forClass(FeedbackExtracao.class);
		verify(feedbackExtracaoRepositoryGateway).salvar(captor.capture());
		assertTrue(captor.getValue().corrigido());
	}

	@Test
	void deveLancarExcecaoQuandoReceitaNaoExiste() {
		UUID id = UUID.randomUUID();
		when(receitaRepositoryGateway.buscarPorId(id)).thenReturn(Optional.empty());

		assertThrows(
				ReceitaNaoEncontradaException.class,
				() -> useCase.executar(id, new ConfirmarReceitaInput("Losartana", "50mg", 24, 30)));
	}
}
