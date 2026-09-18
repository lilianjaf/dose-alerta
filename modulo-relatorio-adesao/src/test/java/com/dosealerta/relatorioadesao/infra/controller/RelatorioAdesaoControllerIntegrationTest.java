package com.dosealerta.relatorioadesao.infra.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class RelatorioAdesaoControllerIntegrationTest {

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
	void deveCalcularATaxaDeAdesaoAposReceberInteracoes() throws Exception {
		UUID pacienteId = UUID.randomUUID();
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");
		registrarInteracao(pacienteId, "Losartana", "NAO_CONFIRMACAO");
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");

		mockMvc.perform(get("/pacientes/{id}/adesao", pacienteId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].medicamento").value("Losartana"))
				.andExpect(jsonPath("$[0].totalConfirmados").value(2))
				.andExpect(jsonPath("$[0].totalNaoConfirmados").value(1))
				.andExpect(jsonPath("$[0].taxaConfirmacao").value(2.0 / 3.0));
	}

	@Test
	void deveFiltrarPorPeriodoQuandoInicioEFimSaoInformados() throws Exception {
		UUID pacienteId = UUID.randomUUID();
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");

		mockMvc.perform(get("/pacientes/{id}/adesao", pacienteId)
						.param("inicio", Instant.now().minusSeconds(3600).toString())
						.param("fim", Instant.now().plusSeconds(3600).toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].totalConfirmados").value(1));
	}

	@Test
	void deveRetornarListaVaziaParaPacienteSemInteracoes() throws Exception {
		mockMvc.perform(get("/pacientes/{id}/adesao", UUID.randomUUID()))
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
