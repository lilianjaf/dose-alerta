package com.dosealerta.ia.infra.client;

import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.PACIENTE_ID;
import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.exception.SchedulerIndisponivelException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpSchedulerClientGatewayTest extends TesteUnitarioBase {

	private MockRestServiceServer servidorMock;
	private HttpSchedulerClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-scheduler");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpSchedulerClientGateway(builder.build());
	}

	@Test
	void deveCriarAlarmeComOPayloadCorreto() {
		UUID pacienteId = PACIENTE_ID;
		Instant horario = INSTANTE_FIXO;

		servidorMock
				.expect(requestTo("http://modulo-scheduler/alarmes"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.pacienteId").value(pacienteId.toString()))
				.andExpect(jsonPath("$.telefone").value(TELEFONE))
				.andExpect(jsonPath("$.medicamento").value("Losartana"))
				.andExpect(jsonPath("$.dose").value("50mg"))
				.andRespond(withSuccess());

		gateway.criarAlarme(pacienteId, TELEFONE, "Losartana", "50mg", horario);

		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoSchedulerFalha() {
		servidorMock.expect(requestTo("http://modulo-scheduler/alarmes")).andRespond(withServerError());

		assertThrows(
				SchedulerIndisponivelException.class,
				() -> gateway.criarAlarme(PACIENTE_ID, TELEFONE, "Losartana", "50mg", INSTANTE_FIXO));
	}
}
