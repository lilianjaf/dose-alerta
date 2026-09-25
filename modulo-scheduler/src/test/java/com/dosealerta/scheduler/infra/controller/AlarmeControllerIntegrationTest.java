package com.dosealerta.scheduler.infra.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AlarmeControllerIntegrationTest {

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

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void deveCriarERecuperarAlarme() throws Exception {
		var input = new CriarAlarmeInput(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now());

		String resposta = mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.medicamento").value("Losartana"))
				.andExpect(jsonPath("$.status").value("PENDENTE"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID id = UUID.fromString(objectMapper.readTree(resposta).get("id").asText());

		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.medicamento").value("Losartana"));
	}

	@Test
	void deveRetornar404ParaAlarmeInexistente() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
	}

	@Test
	void deveRejeitarCriacaoComCamposInvalidos() throws Exception {
		var input = new CriarAlarmeInput(null, "", "", "", null);

		mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar404AoConfirmarAlarmeAindaNaoEnviado() throws Exception {
		String telefone = "+5511988887777";
		var criacao = new CriarAlarmeInput(UUID.randomUUID(), telefone, "Losartana", "50mg", Instant.now());
		mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(criacao)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/alarmes/confirmacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"%s\"}".formatted(telefone)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar404AoConfirmarTelefoneSemAlarmePendente() throws Exception {
		mockMvc.perform(post("/alarmes/confirmacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"+5511900000000\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarConfirmacaoComTelefoneInvalido() throws Exception {
		mockMvc.perform(post("/alarmes/confirmacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveExporEndpointDeHealthCheck() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void deveExporMetricasNoFormatoPrometheus() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}

	@Test
	void devePropagarOCorrelationIdRecebidoNoHeaderDeResposta() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", UUID.randomUUID())
						.header("X-Correlation-Id", "teste-123")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().string("X-Correlation-Id", "teste-123"));
	}

	@Test
	void deveGerarUmCorrelationIdQuandoAusente() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().exists("X-Correlation-Id"));
	}
}
