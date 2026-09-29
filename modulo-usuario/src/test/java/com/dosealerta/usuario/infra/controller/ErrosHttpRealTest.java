package com.dosealerta.usuario.infra.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.usuario.TesteIntegracaoBase;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrosHttpRealTest extends TesteIntegracaoBase {

	private static final String URL_BASE = "http://localhost:";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String JSON = "application/json";
	private static final String PROBLEM_JSON = "application/problem+json";
	private static final String CADASTRO_INVALIDO = "{\"nome\":\"\",\"telefone\":\"abc\",\"senha\":\"123\"}";
	private static final String JSON_MALFORMADO = "{oops";
	private static final String LOGIN_INVALIDO = "{\"telefone\":\"+5511999990000\",\"senha\":\"senha-errada\"}";

	@LocalServerPort
	private int porta;

	private final HttpClient http = HttpClient.newHttpClient();

	private HttpResponse<String> post(String caminho, String json) throws Exception {
		HttpRequest requisicao = HttpRequest.newBuilder(URI.create(URL_BASE + porta + caminho))
				.header(CONTENT_TYPE, JSON)
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();
		return http.send(requisicao, HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void deveRetornarProblemDetail400QuandoOPayloadDoCadastroEInvalido() throws Exception {
		HttpResponse<String> resposta = post("/pacientes", CADASTRO_INVALIDO);

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.headers().firstValue(CONTENT_TYPE).orElse("").contains(PROBLEM_JSON));
		assertTrue(resposta.body().contains("nome"), resposta.body());
	}

	@Test
	void deveRetornarProblemDetail400QuandoOJsonDoCadastroEMalformado() throws Exception {
		HttpResponse<String> resposta = post("/pacientes", JSON_MALFORMADO);

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.headers().firstValue(CONTENT_TYPE).orElse("").contains(PROBLEM_JSON));
	}

	@Test
	void deveRetornarProblemDetail401ParaCredenciaisInvalidas() throws Exception {
		HttpResponse<String> resposta = post("/auth/login", LOGIN_INVALIDO);

		assertEquals(401, resposta.statusCode());
		assertTrue(resposta.headers().firstValue(CONTENT_TYPE).orElse("").contains(PROBLEM_JSON));
	}
}
