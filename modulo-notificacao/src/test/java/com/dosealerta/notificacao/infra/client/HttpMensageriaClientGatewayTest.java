package com.dosealerta.notificacao.infra.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.exception.MensageriaIndisponivelException;
import com.dosealerta.notificacao.core.rules.RegraConteudoNotificacao;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpMensageriaClientGatewayTest {

	private MockRestServiceServer servidorMock;
	private HttpMensageriaClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-mensageria");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpMensageriaClientGateway(builder.build());
	}

	private OutboxEvent evento(Canal canal, EtapaEscalonamento etapa) {
		return OutboxEvent.novo(
				UUID.randomUUID(), UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", etapa, canal, Instant.now());
	}

	@Test
	void deveEnviarMensagemComTextoEBotaoQuandoCanalMensagem() {
		OutboxEvent evento = evento(Canal.MENSAGEM, EtapaEscalonamento.LEMBRETE_INICIAL);

		servidorMock
				.expect(requestTo("http://modulo-mensageria/mensagens/enviar"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value(evento.telefone()))
				.andExpect(jsonPath("$.texto").exists())
				.andExpect(jsonPath("$.textoBotao").value(RegraConteudoNotificacao.TEXTO_BOTAO_CONFIRMACAO))
				.andRespond(withSuccess());

		gateway.enviar(evento);

		servidorMock.verify();
	}

	@Test
	void deveRealizarLigacaoComTextoFaladoQuandoCanalLigacao() {
		OutboxEvent evento = evento(Canal.LIGACAO, EtapaEscalonamento.LIGACAO);

		servidorMock
				.expect(requestTo("http://modulo-mensageria/ligacoes/realizar"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value(evento.telefone()))
				.andExpect(jsonPath("$.textoFalado").exists())
				.andRespond(withSuccess());

		gateway.enviar(evento);

		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoMensageriaFalha() {
		OutboxEvent evento = evento(Canal.MENSAGEM, EtapaEscalonamento.LEMBRETE_INICIAL);

		servidorMock.expect(requestTo("http://modulo-mensageria/mensagens/enviar")).andRespond(withServerError());

		assertThrows(MensageriaIndisponivelException.class, () -> gateway.enviar(evento));
	}
}
