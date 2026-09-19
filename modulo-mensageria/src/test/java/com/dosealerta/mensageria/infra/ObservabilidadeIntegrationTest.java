package com.dosealerta.mensageria.infra;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Único teste do módulo que sobe o contexto Spring completo (os demais são unitários) — cobre
 * a Etapa 9.1 (correlation-id), 9.3 (métricas) e 9.5 (health check), que dependem da
 * infraestrutura web real.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ObservabilidadeIntegrationTest {

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) {
		registry.add("twilio.account-sid", () -> "ACtest0000000000000000000000000000");
		registry.add("twilio.auth-token", () -> "test-auth-token");
		registry.add("twilio.whatsapp-number", () -> "+5511999999999");
		registry.add("twilio.voice-number", () -> "+5511999999999");
	}

	@Autowired
	private MockMvc mockMvc;

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
	void deveGerarUmCorrelationIdQuandoAusente() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(header().exists("X-Correlation-Id"));
	}

	@Test
	void deveExporMetricasNoFormatoPrometheus() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}
}
