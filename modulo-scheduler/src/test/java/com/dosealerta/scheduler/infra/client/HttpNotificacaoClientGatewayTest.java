package com.dosealerta.scheduler.infra.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.exception.NotificacaoIndisponivelException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpNotificacaoClientGatewayTest {

	private MockRestServiceServer servidorMock;
	private HttpNotificacaoClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-notificacao");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpNotificacaoClientGateway(builder.build());
	}

	@Test
	void deveSolicitarEnvioComOPayloadCorreto() {
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "Losartana", "50mg", Instant.now());

		servidorMock
				.expect(requestTo("http://modulo-notificacao/notificacoes/solicitar-envio"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.alarmeId").value(alarme.getId().toString()))
				.andExpect(jsonPath("$.pacienteId").value(alarme.getPacienteId().toString()))
				.andExpect(jsonPath("$.etapa").value("LEMBRETE_INICIAL"))
				.andRespond(withSuccess());

		gateway.solicitarEnvio(alarme, EtapaEscalonamento.LEMBRETE_INICIAL);

		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoNotificacaoFalha() {
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "Losartana", "50mg", Instant.now());

		servidorMock
				.expect(requestTo("http://modulo-notificacao/notificacoes/solicitar-envio"))
				.andRespond(withServerError());

		assertThrows(
				NotificacaoIndisponivelException.class,
				() -> gateway.solicitarEnvio(alarme, EtapaEscalonamento.LEMBRETE_INICIAL));
	}
}
