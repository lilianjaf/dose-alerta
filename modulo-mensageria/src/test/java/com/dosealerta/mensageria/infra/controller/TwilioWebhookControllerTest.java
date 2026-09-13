package com.dosealerta.mensageria.infra.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TwilioWebhookControllerTest {

	private final TwilioWebhookController controller = new TwilioWebhookController();

	@Test
	void deveConfirmarQuandoDigitoCorretoRecebido() {
		String twiml = controller.receberConfirmacaoLigacao("+5511999999999", "CA123", "1");

		assertTrue(twiml.contains("Confirmação registrada"));
	}

	@Test
	void deveInformarRespostaNaoReconhecidaQuandoDigitoErrado() {
		String twiml = controller.receberConfirmacaoLigacao("+5511999999999", "CA123", "9");

		assertTrue(twiml.contains("Não reconhecemos"));
	}

	@Test
	void deveInformarRespostaNaoReconhecidaQuandoSemDigito() {
		String twiml = controller.receberConfirmacaoLigacao("+5511999999999", "CA123", null);

		assertTrue(twiml.contains("Não reconhecemos"));
	}

	@Test
	void naoDeveLancarExcecaoAoReceberRespostaDeTextoOuStatusDeLigacao() {
		controller.receberResposta("+5511999999999", "Confirmo", null);
		controller.receberStatusLigacao("CA123", "completed", "+5511999999999");
	}
}
