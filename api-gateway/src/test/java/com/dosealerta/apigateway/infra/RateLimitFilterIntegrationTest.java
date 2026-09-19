package com.dosealerta.apigateway.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Isolado do {@link GatewayRoutingIntegrationTest} porque exige uma capacidade bem menor que a
 * padrão de produção (Etapa 10.2) — misturar as duas na mesma classe faria os demais testes
 * ali competirem pelo mesmo balde (uma classe = um contexto Spring = um estado de
 * {@code RateLimitFilter} compartilhado, já que o TestRestTemplate sempre bate do mesmo IP).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class RateLimitFilterIntegrationTest {

	private static final int CAPACIDADE = 3;

	private static HttpServer stubModuloUsuario;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeAll
	static void subirStub() throws Exception {
		stubModuloUsuario = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		stubModuloUsuario.createContext("/", exchange -> {
			byte[] corpo = "{\"ok\":true}".getBytes();
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, corpo.length);
			exchange.getResponseBody().write(corpo);
			exchange.close();
		});
		stubModuloUsuario.start();
	}

	@AfterAll
	static void pararStub() {
		stubModuloUsuario.stop(0);
	}

	@DynamicPropertySource
	static void propriedades(DynamicPropertyRegistry registry) throws Exception {
		registry.add("modulo-usuario.uri", () -> "http://localhost:" + stubModuloUsuario.getAddress().getPort());
		registry.add("app.rate-limit.capacidade", () -> CAPACIDADE);
		registry.add("app.rate-limit.janela-ms", () -> 60_000);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	@Test
	void deveBloquearAposExcederACapacidadeNaJanela() {
		for (int i = 0; i < CAPACIDADE; i++) {
			ResponseEntity<String> resposta = restTemplate.postForEntity("/pacientes", "{}", String.class);
			assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		ResponseEntity<String> excedente = restTemplate.postForEntity("/pacientes", "{}", String.class);
		assertThat(excedente.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
	}

	@Test
	void naoDeveContarActuatorHealthNaCapacidade() {
		for (int i = 0; i < CAPACIDADE + 5; i++) {
			ResponseEntity<String> resposta = restTemplate.getForEntity("/actuator/health", String.class);
			assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
	}
}
