package com.dosealerta.ia.infra.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ReceitaControllerIntegrationTest {

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

	@MockitoBean
	private ExtratorReceitaGateway extratorReceitaGateway;

	@MockitoBean
	private SchedulerClientGateway schedulerClientGateway;

	@Test
	void deveExtrairPersistirEConfirmarUmaReceita() throws Exception {
		when(extratorReceitaGateway.extrair(any())).thenReturn(new ReceitaExtraida("Losartana", "50mg", 24, 30));

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.medicamento").value("Losartana"))
				.andExpect(jsonPath("$.status").value("AGUARDANDO_CONFIRMACAO"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID id = UUID.fromString(objectMapper.readTree(resposta).get("id").asText());

		mockMvc.perform(get("/receitas/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(jsonPath("$.medicamento").value("Losartana"));

		String corpoConfirmacao =
				"{\"medicamento\":\"Losartana\",\"dose\":\"50mg\",\"frequenciaHoras\":24,\"duracaoDias\":30}";
		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoConfirmacao))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"));
	}

	@Test
	void deveRetornar404ParaReceitaInexistente() throws Exception {
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
	}

	@Test
	void deveRejeitarExtracaoSemImagem() throws Exception {
		var imagemVazia = new MockMultipartFile("imagem", "vazia.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagemVazia)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRejeitarExtracaoComTelefoneInvalido() throws Exception {
		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "numero-invalido")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar422QuandoGuardrailReprovaAExtracao() throws Exception {
		when(extratorReceitaGateway.extrair(any())).thenReturn(new ReceitaExtraida("Losartana", "dose-invalida", 24, 30));

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void deveExporEndpointDeHealthCheck() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void devePropagarOCorrelationIdRecebidoNoHeaderDeResposta() throws Exception {
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID())
						.header("X-Correlation-Id", "teste-123")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().string("X-Correlation-Id", "teste-123"));
	}

	@Test
	void deveExporMetricasNoFormatoPrometheus() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}
}
