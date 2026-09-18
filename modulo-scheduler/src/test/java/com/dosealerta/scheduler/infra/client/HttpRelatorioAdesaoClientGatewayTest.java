package com.dosealerta.scheduler.infra.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import com.dosealerta.scheduler.core.exception.RelatorioAdesaoIndisponivelException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpRelatorioAdesaoClientGatewayTest {

	private MockRestServiceServer servidorMock;
	private HttpRelatorioAdesaoClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-relatorio-adesao");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpRelatorioAdesaoClientGateway(builder.build());
	}

	@Test
	void deveRegistrarInteracaoComOPayloadCorreto() {
		EventoInteracao evento = EventoInteracao.novo(
				UUID.randomUUID(), UUID.randomUUID(), "Losartana", TipoInteracao.CONFIRMACAO, Instant.now());

		servidorMock
				.expect(requestTo("http://modulo-relatorio-adesao/interacoes"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.id").value(evento.id().toString()))
				.andExpect(jsonPath("$.alarmeId").value(evento.alarmeId().toString()))
				.andExpect(jsonPath("$.pacienteId").value(evento.pacienteId().toString()))
				.andExpect(jsonPath("$.medicamento").value("Losartana"))
				.andExpect(jsonPath("$.tipo").value("CONFIRMACAO"))
				.andRespond(withSuccess());

		gateway.registrarInteracao(evento);

		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoRelatorioAdesaoFalha() {
		EventoInteracao evento = EventoInteracao.novo(
				UUID.randomUUID(), UUID.randomUUID(), "Losartana", TipoInteracao.CONFIRMACAO, Instant.now());
		servidorMock.expect(requestTo("http://modulo-relatorio-adesao/interacoes")).andRespond(withServerError());

		assertThrows(RelatorioAdesaoIndisponivelException.class, () -> gateway.registrarInteracao(evento));
	}
}
