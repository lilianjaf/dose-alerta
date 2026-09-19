package com.dosealerta.mensageria.infra;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Único teste do módulo que sobe o contexto Spring completo (os demais são unitários) — cobre
 * a Etapa 9.1 (correlation-id), 9.3 (métricas), 9.5 (health check) e 10.5 (assinatura dos
 * webhooks do Twilio), que dependem da infraestrutura web real.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ObservabilidadeIntegrationTest {

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("twilio.account-sid", () -> "ACtest0000000000000000000000000000");
		registry.add("twilio.auth-token", () -> "test-auth-token");
		registry.add("twilio.whatsapp-number", () -> "+5511999999999");
		registry.add("twilio.voice-number", () -> "+5511999999999");
		registry.add("twilio.webhook-base-url", () -> "http://localhost:8085");

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	private static final String AUTH_TOKEN = "test-auth-token";
	private static final String URL_WEBHOOK = "http://localhost:8085/webhooks/twilio/mensagens";

	@Autowired
	private MockMvc mockMvc;

	private static String assinar(String url, Map<String, String> parametros) throws Exception {
		var ordenados = new TreeMap<>(parametros);
		StringBuilder dados = new StringBuilder(url);
		ordenados.forEach((chave, valor) -> dados.append(chave).append(valor));

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(new SecretKeySpec(AUTH_TOKEN.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
		byte[] hash = mac.doFinal(dados.toString().getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(hash);
	}

	@Test
	void deveRejeitarWebhookSemAssinatura() throws Exception {
		MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
		corpo.add("From", "+5511999999999");
		corpo.add("Body", "oi");

		mockMvc.perform(post("/webhooks/twilio/mensagens").contentType(MediaType.APPLICATION_FORM_URLENCODED).params(corpo))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveRejeitarWebhookComAssinaturaInvalida() throws Exception {
		MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
		corpo.add("From", "+5511999999999");
		corpo.add("Body", "oi");

		mockMvc.perform(post("/webhooks/twilio/mensagens")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.header("X-Twilio-Signature", "assinatura-forjada")
						.params(corpo))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveAceitarWebhookComAssinaturaValida() throws Exception {
		Map<String, String> parametros = Map.of("From", "+5511999999999", "Body", "oi");
		String assinatura = assinar(URL_WEBHOOK, parametros);

		MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
		parametros.forEach(corpo::add);

		mockMvc.perform(post("/webhooks/twilio/mensagens")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.header("X-Twilio-Signature", assinatura)
						.params(corpo))
				.andExpect(status().isOk());
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
