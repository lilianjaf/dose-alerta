package com.dosealerta.mensageria.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;
import com.dosealerta.mensageria.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.mensageria.core.exception.PacienteIndisponivelException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpPacienteClientGatewayTest {

	private MockRestServiceServer servidorMock;
	private HttpPacienteClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-usuario");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpPacienteClientGateway(builder.build());
	}

	@Test
	void deveIdentificarOPaciente() {
		UUID pacienteId = UUID.randomUUID();
		servidorMock
				.expect(requestTo("http://modulo-usuario/pacientes/identificar"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value("+5511999999999"))
				.andRespond(withSuccess(
						"{\"pacienteId\":\"" + pacienteId
								+ "\",\"nome\":\"Maria\",\"cadastroCompleto\":true,\"recemCriado\":false}",
						MediaType.APPLICATION_JSON));

		IdentificarPacienteResultado resultado = gateway.identificar("+5511999999999");

		assertEquals(pacienteId, resultado.pacienteId());
		assertEquals("Maria", resultado.nome());
		assertEquals(true, resultado.cadastroCompleto());
		assertEquals(false, resultado.recemCriado());
	}

	@Test
	void deveCompletarOCadastroComOPayloadCorreto() {
		servidorMock
				.expect(requestTo("http://modulo-usuario/pacientes/completar-cadastro"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value("+5511999999999"))
				.andExpect(jsonPath("$.numeroInscricaoSus").value("700000000000001"))
				.andRespond(withSuccess());

		gateway.completarCadastro("+5511999999999", "700000000000001");

		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoONumeroDeInscricaoNaoEEncontrado() {
		servidorMock
				.expect(requestTo("http://modulo-usuario/pacientes/completar-cadastro"))
				.andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT));

		assertThrows(
				NumeroInscricaoSusNaoEncontradoException.class,
				() -> gateway.completarCadastro("+5511999999999", "999999999999999"));
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoONumeroDeInscricaoNaoEUmNumeroValido() {

		servidorMock
				.expect(requestTo("http://modulo-usuario/pacientes/completar-cadastro"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST));

		assertThrows(
				NumeroInscricaoSusNaoEncontradoException.class,
				() -> gateway.completarCadastro("+5511999999999", "oi"));
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoOUsuarioFalha() {
		servidorMock.expect(requestTo("http://modulo-usuario/pacientes/identificar")).andRespond(withServerError());

		assertThrows(PacienteIndisponivelException.class, () -> gateway.identificar("+5511999999999"));
	}
}
