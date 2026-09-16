package com.dosealerta.mensageria.infra.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;
import com.dosealerta.mensageria.core.gateway.LigacaoGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class EnvioControllerTest {

	@Mock
	private MensageriaGateway mensageriaGateway;

	@Mock
	private LigacaoGateway ligacaoGateway;

	private EnvioController controller;

	@BeforeEach
	void setUp() {
		controller = new EnvioController(mensageriaGateway, ligacaoGateway);
	}

	@Test
	void deveEnviarMensagemComBotaoDeConfirmacao() {
		var request = new EnviarMensagemRequest("+5511999999999", "Hora de tomar Losartana", "CONFIRMAR");

		var resposta = controller.enviarMensagem(request);

		assertEquals(HttpStatus.ACCEPTED, resposta.getStatusCode());
		verify(mensageriaGateway)
				.enviarMensagem(
						new ContatoWhatsApp("+5511999999999"),
						new ConteudoMensagem.ComBotaoConfirmacao("Hora de tomar Losartana", "CONFIRMAR"));
	}

	@Test
	void deveRealizarLigacaoComTextoFalado() {
		var request = new RealizarLigacaoRequest("+5511999999999", "Aperte 1 para confirmar");

		var resposta = controller.realizarLigacao(request);

		assertEquals(HttpStatus.ACCEPTED, resposta.getStatusCode());
		verify(ligacaoGateway)
				.ligarParaConfirmar(
						new ContatoWhatsApp("+5511999999999"), new SolicitacaoLigacao("Aperte 1 para confirmar"));
	}
}
