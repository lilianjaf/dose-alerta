package com.dosealerta.usuario.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpCadastroSusGatewayTest {

	private MockRestServiceServer servidorMock;
	private HttpCadastroSusGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://sus-mock");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpCadastroSusGateway(builder.build());
	}

	@Test
	void deveDevolverONomeQuandoOSusAchaOTelefone() {
		servidorMock
				.expect(requestTo("http://sus-mock/sus/cadastros/%2B5511999990001"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"telefone\":\"+5511999990001\",\"nomeCompleto\":\"Ana Paula Souza\"}", MediaType.APPLICATION_JSON));

		Optional<String> resultado = gateway.buscarNomePorTelefone("+5511999990001");

		assertTrue(resultado.isPresent());
		assertEquals("Ana Paula Souza", resultado.get());
	}

	@Test
	void deveDevolverVazioQuandoOSusRespondeNaoEncontrado() {
		servidorMock
				.expect(requestTo("http://sus-mock/sus/cadastros/%2B5511988887777"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertTrue(gateway.buscarNomePorTelefone("+5511988887777").isEmpty());
	}

	@Test
	void deveDevolverVazioEmVezDePropagarQuandoOSusEstaFora() {
		servidorMock
				.expect(requestTo("http://sus-mock/sus/cadastros/%2B5511988887777"))
				.andRespond(withServerError());

		assertTrue(gateway.buscarNomePorTelefone("+5511988887777").isEmpty());
	}

	@Test
	void deveDevolverONomeQuandoOSusAchaONumeroDeInscricao() {
		servidorMock
				.expect(requestTo("http://sus-mock/sus/inscricoes/700000000000001"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"numeroInscricao\":\"700000000000001\",\"nomeCompleto\":\"Fernanda Torres Lima\"}",
						MediaType.APPLICATION_JSON));

		Optional<String> resultado = gateway.buscarNomePorNumeroInscricao("700000000000001");

		assertTrue(resultado.isPresent());
		assertEquals("Fernanda Torres Lima", resultado.get());
	}

	@Test
	void deveDevolverVazioQuandoOSusNaoAchaONumeroDeInscricao() {
		servidorMock
				.expect(requestTo("http://sus-mock/sus/inscricoes/999999999999999"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertTrue(gateway.buscarNomePorNumeroInscricao("999999999999999").isEmpty());
	}
}
