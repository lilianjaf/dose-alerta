package com.dosealerta.relatorioadesao.infra.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class InteracaoControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveAceitarRegistroDeInteracaoValida() throws Exception {
		String corpo =
				"""
				{
				  "id": "%s",
				  "alarmeId": "%s",
				  "pacienteId": "%s",
				  "medicamento": "Losartana",
				  "tipo": "CONFIRMACAO",
				  "registradaEm": "%s"
				}
				"""
						.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now());

		mockMvc.perform(post("/interacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isAccepted());
	}

	@Test
	void deveRejeitarInteracaoComCamposInvalidos() throws Exception {
		mockMvc.perform(post("/interacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"medicamento\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveExporEndpointDeHealthCheck() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void devePropagarOCorrelationIdRecebidoNoHeaderDeResposta() throws Exception {
		mockMvc.perform(get("/actuator/health").header("X-Correlation-Id", "teste-123"))
				.andExpect(header().string("X-Correlation-Id", "teste-123"));
	}

	@Test
	void deveExporMetricasNoFormatoPrometheus() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}
}
