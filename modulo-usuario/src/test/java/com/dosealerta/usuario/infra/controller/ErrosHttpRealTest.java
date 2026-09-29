package com.dosealerta.usuario.infra.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrosHttpRealTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
		registry.add(
				"security.jwt.private-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPrivate().getEncoded()));
	}

	@LocalServerPort
	private int porta;

	private final HttpClient http = HttpClient.newHttpClient();

	private HttpResponse<String> post(String caminho, String json) throws Exception {
		HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();
		return http.send(requisicao, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void deveRetornar400ComMensagemQuandoOPayloadDoCadastroEInvalido() throws Exception {
		HttpResponse<String> resposta = post("/pacientes", "{\"nome\":\"\",\"telefone\":\"abc\",\"senha\":\"123\"}");

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("telefone"), resposta.body());
	}

	@Test
	void deveRetornar400QuandoOJsonDoCadastroEMalformado() throws Exception {
		assertEquals(400, post("/pacientes", "{oops").statusCode());
	}

	@Test
	void deveContinuarRetornando401ParaCredenciaisInvalidas() throws Exception {
		HttpResponse<String> resposta =
				post("/auth/login", "{\"telefone\":\"+5511999990000\",\"senha\":\"senha-errada\"}");

		assertEquals(401, resposta.statusCode());
	}
}
