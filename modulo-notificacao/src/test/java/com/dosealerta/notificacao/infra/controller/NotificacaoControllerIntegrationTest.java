package com.dosealerta.notificacao.infra.controller;

import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoCom;
import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoValida;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.notificacao.TesteIntegracaoBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class NotificacaoControllerIntegrationTest extends TesteIntegracaoBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void deveAceitarSolicitacaoDeEnvioValida() throws Exception {
		var input = umaSolicitacaoValida();

		mockMvc.perform(post("/notificacoes/solicitar-envio")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
				.andExpect(status().isAccepted());
	}

	@Test
	void deveRejeitarSolicitacaoComCamposInvalidos() throws Exception {
		var input = umaSolicitacaoCom(null, null, "", "", "", null);

		mockMvc.perform(post("/notificacoes/solicitar-envio")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(input)))
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
