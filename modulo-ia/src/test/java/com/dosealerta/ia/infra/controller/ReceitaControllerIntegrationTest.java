package com.dosealerta.ia.infra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

		mockMvc.perform(get("/receitas/{id}", id)).andExpect(jsonPath("$.medicamento").value("Losartana"));

		String corpoConfirmacao =
				"{\"medicamento\":\"Losartana\",\"dose\":\"50mg\",\"frequenciaHoras\":24,\"duracaoDias\":30}";
		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoConfirmacao))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"));
	}

	@Test
	void deveRetornar404ParaReceitaInexistente() throws Exception {
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarExtracaoSemImagem() throws Exception {
		var imagemVazia = new MockMultipartFile("imagem", "vazia.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagemVazia)
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
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isUnprocessableEntity());
	}
}
