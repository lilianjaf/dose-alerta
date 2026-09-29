package com.dosealerta.mensageria.infra.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.exception.AlarmeIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpAlarmeClientGatewayTest extends TesteUnitarioBase {

	private MockRestServiceServer servidorMock;
	private HttpAlarmeClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-scheduler");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpAlarmeClientGateway(builder.build());
	}

	@Test
	void deveRegistrarConfirmacaoComOTelefoneCorreto() {
		servidorMock
				.expect(requestTo("http://modulo-scheduler/alarmes/confirmacoes"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value("+5511999999999"))
				.andRespond(withSuccess());

		gateway.registrarConfirmacao("+5511999999999");

		servidorMock.verify();
	}

	@Test
	void deveRegistrarLigacaoAtendidaComOTelefoneCorreto() {
		servidorMock
				.expect(requestTo("http://modulo-scheduler/alarmes/ligacoes/atendidas"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value("+5511999999999"))
				.andRespond(withSuccess());

		gateway.registrarLigacaoAtendida("+5511999999999");

		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoModuloSchedulerFalha() {
		servidorMock
				.expect(requestTo("http://modulo-scheduler/alarmes/confirmacoes"))
				.andRespond(withServerError());

		assertThrows(AlarmeIndisponivelException.class, () -> gateway.registrarConfirmacao("+5511999999999"));
	}
}
