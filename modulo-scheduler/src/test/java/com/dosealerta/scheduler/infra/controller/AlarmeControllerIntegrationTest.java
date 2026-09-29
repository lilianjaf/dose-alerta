package com.dosealerta.scheduler.infra.controller;

import static com.dosealerta.scheduler.SchedulerFixtures.ALARME_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.OUTRO_PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoCom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.scheduler.TesteIntegracaoBase;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class AlarmeControllerIntegrationTest extends TesteIntegracaoBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void deveCriarERecuperarAlarme() throws Exception {
		var input = umaCriacaoCom(PACIENTE_ID, "+5511999999999", "Losartana", "50mg", INSTANTE_FIXO);

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
		mockMvc.perform(get("/alarmes/{id}", ALARME_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", ALARME_ID)).andExpect(status().isUnauthorized());
	}

	@Test
	void naoDeveDuplicarAlarmePendenteDoMesmoMedicamento() throws Exception {
		UUID pacienteId = PACIENTE_ID;
		var input = umaCriacaoCom(pacienteId, "+5511999999999", "Aerolin spray 100 mcg", "2 doses", INSTANTE_FIXO);

		String primeira = criarAlarme(input, status().isCreated());
		String repetida = criarAlarme(
				umaCriacaoCom(
						pacienteId, "+5511999999999", "AEROLIN SPRAY 100 MCG", "2 doses", INSTANTE_FIXO.plusSeconds(3600)),
				status().isOk());

		assertEquals(objectMapper.readTree(primeira).get("id"), objectMapper.readTree(repetida).get("id"));
	}

	@Test
	void deveCriarAlarmeParaMedicamentoDiferenteOuPacienteDiferente() throws Exception {
		UUID pacienteId = PACIENTE_ID;
		criarAlarme(umaCriacaoCom(pacienteId, "+5511999999999", "Losartana", "50mg", INSTANTE_FIXO), status().isCreated());

		criarAlarme(umaCriacaoCom(pacienteId, "+5511999999999", "Aerolin", "2 doses", INSTANTE_FIXO), status().isCreated());
		criarAlarme(
				umaCriacaoCom(OUTRO_PACIENTE_ID, "+5511988887777", "Losartana", "50mg", INSTANTE_FIXO),
				status().isCreated());
	}

	@Test
	void deveRejeitarCriacaoComCamposInvalidos() throws Exception {
		var input = umaCriacaoCom(null, "", "", "", null);

		mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void devePermitirConfirmarUmAlarmeAindaNaoEnviadoPeloJobDeEscalonamento() throws Exception {
		String telefone = "+5511988887777";
		var criacao = umaCriacaoCom(PACIENTE_ID, telefone, "Losartana", "50mg", INSTANTE_FIXO);
		mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(criacao)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/alarmes/confirmacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"%s\"}".formatted(telefone)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));
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
		mockMvc.perform(get("/alarmes/{id}", ALARME_ID)
						.header("X-Correlation-Id", "teste-123")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().string("X-Correlation-Id", "teste-123"));
	}

	@Test
	void deveGerarUmCorrelationIdQuandoAusente() throws Exception {
		mockMvc.perform(get("/alarmes/{id}", ALARME_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().exists("X-Correlation-Id"));
	}

	private String criarAlarme(CriarAlarmeInput input, ResultMatcher esperado)
			throws Exception {
		return mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(esperado)
				.andReturn()
				.getResponse()
				.getContentAsString();
	}
}
