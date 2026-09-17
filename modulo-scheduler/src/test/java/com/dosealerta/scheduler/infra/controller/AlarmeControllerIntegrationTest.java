package com.dosealerta.scheduler.infra.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
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
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AlarmeControllerIntegrationTest {

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

		mockMvc.perform(get("/alarmes/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.medicamento").value("Losartana"));
	}

	@Test
	void deveRetornar404ParaAlarmeInexistente() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
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

		// O alarme existe, mas nenhuma etapa foi enviada ainda — não há resposta a correlacionar.
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
}
