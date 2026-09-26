package com.dosealerta.ia.infra.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Usa um servidor HTTP de verdade: o MockMvc não simula o reencaminhamento de erros para /error feito pelo
 * container, que era justamente o que transformava todo 400 do Spring em 401.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrosHttpRealTest {

	private static final String LIMITE = "----limite-teste";

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	private static RSAPrivateKey chavePrivada;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	@LocalServerPort
	private int porta;

	@MockitoBean
	private ExtratorReceitaGateway extratorReceitaGateway;

	@MockitoBean
	private SchedulerClientGateway schedulerClientGateway;

	private final HttpClient http = HttpClient.newHttpClient();

	private static String tokenValido() throws Exception {
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

	private HttpResponse<String> enviar(String caminho, String contentType, String corpo, String token) throws Exception {
		HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
				.header("Content-Type", contentType)
				.POST(HttpRequest.BodyPublishers.ofString(corpo));
		if (token != null) {
			requisicao.header("Authorization", "Bearer " + token);
		}
		return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static String multipartSemImagem() {
		return "--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"pacienteId\"\r\n\r\n" + UUID.randomUUID()
				+ "\r\n--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"telefone\"\r\n\r\n+5511999999999"
				+ "\r\n--" + LIMITE + "--\r\n";
	}

	@Test
	void deveRetornar400ComMensagemQuandoAImagemNaoEEnviada() throws Exception {
		HttpResponse<String> resposta = enviar(
				"/receitas/extrair", "multipart/form-data; boundary=" + LIMITE, multipartSemImagem(), tokenValido());

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("imagem"), resposta.body());
	}

	@Test
	void deveRetornar400QuandoOCorpoDaConfirmacaoEMalformado() throws Exception {
		HttpResponse<String> resposta = enviar(
				"/receitas/" + UUID.randomUUID() + "/confirmar", "application/json", "{oops", tokenValido());

		assertEquals(400, resposta.statusCode());
	}

	@Test
	void deveRetornar400ComOCampoInvalidoNaConfirmacao() throws Exception {
		HttpResponse<String> resposta = enviar(
				"/receitas/" + UUID.randomUUID() + "/confirmar",
				"application/json",
				"{\"frequenciaHoras\":999}",
				tokenValido());

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("frequenciaHoras"), resposta.body());
	}

	@Test
	void deveRetornar400ComMensagemQuandoUmParametroObrigatorioFalta() throws Exception {
		String corpo = "--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"imagem\"; filename=\"r.jpg\"\r\n"
				+ "Content-Type: image/jpeg\r\n\r\nabc\r\n--" + LIMITE
				+ "\r\nContent-Disposition: form-data; name=\"pacienteId\"\r\n\r\n" + UUID.randomUUID()
				+ "\r\n--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"telefone\"\r\n\r\n+5511999999999"
				+ "\r\n--" + LIMITE + "--\r\n";

		HttpResponse<String> resposta =
				enviar("/receitas/extrair", "multipart/form-data; boundary=" + LIMITE, corpo, tokenValido());

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("horarioInicial"), resposta.body());
	}

	@Test
	void deveRetornar415QuandoOExtrairRecebeJsonEmVezDeMultipart() throws Exception {
		HttpResponse<String> resposta = enviar("/receitas/extrair", "application/json", "{}", tokenValido());

		assertEquals(415, resposta.statusCode());
	}

	@Test
	void deveContinuarRetornando401SemTokenOuComTokenInvalido() throws Exception {
		assertEquals(
				401,
				enviar("/receitas/extrair", "multipart/form-data; boundary=" + LIMITE, multipartSemImagem(), null)
						.statusCode());
		assertEquals(
				401,
				enviar(
								"/receitas/extrair",
								"multipart/form-data; boundary=" + LIMITE,
								multipartSemImagem(),
								"token-invalido")
						.statusCode());
	}
}
