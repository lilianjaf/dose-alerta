package com.dosealerta.mensageria.infra.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TwilioWebhookControllerTest {

	@Mock
	private ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase;

	@Mock
	private ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase;

	@Mock
	private ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase;

	private TwilioWebhookController controller;

	@BeforeEach
	void setUp() {

		controller = new TwilioWebhookController(
				processarMensagemRecebidaUseCase,
				processarConfirmacaoLigacaoUseCase,
				processarStatusLigacaoUseCase,
				Runnable::run);
	}

	@Test
	void deveConfirmarQuandoDigitoCorretoRecebido() {
		when(processarConfirmacaoLigacaoUseCase.executar("+5511999999999", "1")).thenReturn(true);

		String twiml = controller.receberConfirmacaoLigacao("+5511999999999", "CA123", "1");

		assertTrue(twiml.contains("Confirmação registrada"));
	}

	@Test
	void deveInformarRespostaNaoReconhecidaQuandoDigitoErrado() {
		when(processarConfirmacaoLigacaoUseCase.executar("+5511999999999", "9")).thenReturn(false);

		String twiml = controller.receberConfirmacaoLigacao("+5511999999999", "CA123", "9");

		assertTrue(twiml.contains("Não reconhecemos"));
	}

	@Test
	void deveInformarRespostaNaoReconhecidaQuandoSemDigito() {
		when(processarConfirmacaoLigacaoUseCase.executar("+5511999999999", null)).thenReturn(false);

		String twiml = controller.receberConfirmacaoLigacao("+5511999999999", "CA123", null);

		assertTrue(twiml.contains("Não reconhecemos"));
	}

	@Test
	void deveRemoverOPrefixoWhatsappDoTelefoneAntesDeDelegarAoUseCase() {

		controller.receberResposta("whatsapp:+5511999999999", "Confirmo", null, null, 0, null);

		verify(processarMensagemRecebidaUseCase)
				.executar(new DadosMensagemRecebida("+5511999999999", "Confirmo", null, null, 0, null));
	}

	@Test
	void deveRepassarOsDadosDeMidiaQuandoAMensagemTemFoto() {
		controller.receberResposta(
				"whatsapp:+5511999999999", null, null, "https://api.twilio.com/media/ME123", 1, "image/jpeg");

		verify(processarMensagemRecebidaUseCase)
				.executar(new DadosMensagemRecebida(
						"+5511999999999", null, null, "https://api.twilio.com/media/ME123", 1, "image/jpeg"));
	}

	@Test
	void deveDelegarStatusDeLigacaoAoUseCase() {
		controller.receberStatusLigacao("CA123", "completed", "+5511999999999");

		verify(processarStatusLigacaoUseCase).executar("+5511999999999", "completed");
	}
}
