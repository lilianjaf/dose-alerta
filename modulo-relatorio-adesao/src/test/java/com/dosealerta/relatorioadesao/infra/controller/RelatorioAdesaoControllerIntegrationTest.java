package com.dosealerta.relatorioadesao.infra.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class RelatorioAdesaoControllerIntegrationTest {

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
				.subject("profissional-1")
				.issueTime(Date.from(agora))
				.expirationTime(Date.from(agora.plusSeconds(3600)))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chavePrivada));
		return signedJWT.serialize();
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveCalcularATaxaDeAdesaoAposReceberInteracoes() throws Exception {
		UUID pacienteId = UUID.randomUUID();
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");
		registrarInteracao(pacienteId, "Losartana", "NAO_CONFIRMACAO");
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");

		mockMvc.perform(get("/pacientes/{id}/adesao", pacienteId).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].medicamento").value("Losartana"))
				.andExpect(jsonPath("$[0].totalConfirmados").value(2))
				.andExpect(jsonPath("$[0].totalNaoConfirmados").value(1))
				.andExpect(jsonPath("$[0].taxaConfirmacao").value(2.0 / 3.0));
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/pacientes/{id}/adesao", UUID.randomUUID())).andExpect(status().isUnauthorized());
	}

	@Test
	void deveFiltrarPorPeriodoQuandoInicioEFimSaoInformados() throws Exception {
		UUID pacienteId = UUID.randomUUID();
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");

		mockMvc.perform(get("/pacientes/{id}/adesao", pacienteId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("inicio", Instant.now().minusSeconds(3600).toString())
						.param("fim", Instant.now().plusSeconds(3600).toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].totalConfirmados").value(1));
	}

	@Test
	void deveRetornarListaVaziaParaPacienteSemInteracoes() throws Exception {
		mockMvc.perform(get("/pacientes/{id}/adesao", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	private void registrarInteracao(UUID pacienteId, String medicamento, String tipo) throws Exception {
		String corpo =
				"""
				{
				  "id": "%s",
				  "alarmeId": "%s",
				  "pacienteId": "%s",
				  "medicamento": "%s",
				  "tipo": "%s",
				  "registradaEm": "%s"
				}
				"""
						.formatted(UUID.randomUUID(), UUID.randomUUID(), pacienteId, medicamento, tipo, Instant.now());

		mockMvc.perform(post("/interacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isAccepted());
	}
}
