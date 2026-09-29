package com.dosealerta.relatorioadesao.infra.controller;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.ALARME_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.INTERACAO_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.relatorioadesao.TesteIntegracaoBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class InteracaoControllerIntegrationTest extends TesteIntegracaoBase {

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
						.formatted(INTERACAO_ID, ALARME_ID, PACIENTE_ID, INSTANTE_FIXO);

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
