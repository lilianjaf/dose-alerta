package com.dosealerta.apigateway.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.dosealerta.apigateway.infra.filter.CorrelationIdFilter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class GatewayRoutingIntegrationTest {

	private static HttpServer stubModuloUsuario;
	private static final AtomicReference<String> correlationIdRecebidoPeloStub = new AtomicReference<>();

	private static RSAPrivateKey chavePrivada;
	private static RSAPublicKey chavePublica;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeAll
	static void subirStubEChaves() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		chavePublica = (RSAPublicKey) chaves.getPublic();

		stubModuloUsuario = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		stubModuloUsuario.createContext("/", exchange -> {
			correlationIdRecebidoPeloStub.set(
					exchange.getRequestHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER));
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
	static void propriedades(DynamicPropertyRegistry registry) {
		registry.add(
				"security.jwt.public-key", () -> Base64.getEncoder().encodeToString(chavePublica.getEncoded()));
		registry.add("modulo-usuario.uri", () -> "http://localhost:" + stubModuloUsuario.getAddress().getPort());
	}

	private String tokenValido() throws Exception {
		Instant agora = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject("paciente-1")
				.issueTime(Date.from(agora))
				.expirationTime(Date.from(agora.plusSeconds(3600)))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chavePrivada));
		return signedJWT.serialize();
	}

	@Test
	void deveRotearRequisicaoPublicaParaModuloUsuarioSemToken() {
		ResponseEntity<String> resposta = restTemplate.postForEntity("/pacientes", "{}", String.class);

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resposta.getBody()).contains("ok");
	}

	@Test
	void deveRejeitarRotaProtegidaSemToken() {
		ResponseEntity<String> resposta = restTemplate.getForEntity("/rota-protegida-qualquer", String.class);

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void deveAceitarRotaProtegidaComTokenValido() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(tokenValido());

		ResponseEntity<String> resposta =
				restTemplate.exchange("/pacientes", HttpMethod.GET, new HttpEntity<>(headers), String.class);

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resposta.getBody()).contains("ok");
	}

	@Test
	void devePermitirPreflightCorsSemAutenticar() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ORIGIN, "http://localhost:3000");
		headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");

		ResponseEntity<String> resposta = restTemplate.exchange(
				"/auth/login", HttpMethod.OPTIONS, new HttpEntity<>(headers), String.class);

		assertThat(resposta.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(resposta.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
				.isEqualTo("http://localhost:3000");
	}

	@Test
	void deveGerarEPropagarCorrelationIdQuandoAusente() {
		ResponseEntity<String> resposta = restTemplate.postForEntity("/pacientes", "{}", String.class);

		String correlationIdNaResposta = resposta.getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
		assertThat(correlationIdNaResposta).isNotBlank();
		assertThat(correlationIdRecebidoPeloStub.get()).isEqualTo(correlationIdNaResposta);
	}

	@Test
	void deveExporEndpointDeHealthCheckSemAutenticar() {
		ResponseEntity<String> resposta = restTemplate.getForEntity("/actuator/health", String.class);

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resposta.getBody()).contains("UP");
	}

	@Test
	void devePreservarCorrelationIdExistente() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(CorrelationIdFilter.CORRELATION_ID_HEADER, "correlation-id-do-cliente");

		ResponseEntity<String> resposta = restTemplate.exchange(
				"/pacientes", HttpMethod.POST, new HttpEntity<>("{}", headers), String.class);

		assertThat(resposta.getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER))
				.isEqualTo("correlation-id-do-cliente");
		assertThat(correlationIdRecebidoPeloStub.get()).isEqualTo("correlation-id-do-cliente");
	}

	@Test
	void deveExporMetricasNoFormatoPrometheusSemAutenticar() {
		ResponseEntity<String> resposta = restTemplate.getForEntity("/actuator/prometheus", String.class);

		assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(resposta.getBody()).contains("jvm_memory_used_bytes");
	}
}
