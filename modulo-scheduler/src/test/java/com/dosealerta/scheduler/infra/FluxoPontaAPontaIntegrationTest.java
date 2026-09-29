package com.dosealerta.scheduler.infra;

import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoCom;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.scheduler.TesteIntegracaoBase;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosPendentesUseCase;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class FluxoPontaAPontaIntegrationTest extends TesteIntegracaoBase {

	private static final String TELEFONE_DO_FLUXO = "+5511977776666";
	private static final int STATUS_ACEITO = 202;
	private static final int TIMEOUT_SEGUNDOS = 5;
	private static final String BEARER = "Bearer ";

	private static final BlockingQueue<String> REQUISICOES_RECEBIDAS = new ArrayBlockingQueue<>(10);
	private static HttpServer stubNotificacao;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EscalonarAlarmesUseCase escalonarAlarmesUseCase;

	@Autowired
	private PublicarEventosPendentesUseCase publicarEventosPendentesUseCase;

	@DynamicPropertySource
	static void propriedadesDoStub(DynamicPropertyRegistry registry) throws Exception {
		stubNotificacao = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		stubNotificacao.createContext("/notificacoes/solicitar-envio", exchange -> {
			try (exchange) {
				String corpo = new String(exchange.getRequestBody().readAllBytes());
				REQUISICOES_RECEBIDAS.offer(corpo);
				exchange.sendResponseHeaders(STATUS_ACEITO, -1);
			}
		});
		stubNotificacao.start();
		registry.add("modulo-notificacao.uri", () -> "http://localhost:" + stubNotificacao.getAddress().getPort());
	}

	@AfterAll
	static void pararStub() {
		stubNotificacao.stop(0);
	}

	@Test
	void deveEscalonarNotificarEConfirmarUmAlarmeDePontaAPonta() throws Exception {
		var criacao = umaCriacaoCom(
				PACIENTE_ID, TELEFONE_DO_FLUXO, "Losartana", "50mg", INSTANTE_FIXO.minus(1, ChronoUnit.MINUTES));

		String resposta = mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(criacao)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		UUID id = UUID.fromString(objectMapper.readTree(resposta).get("id").asText());

		escalonarAlarmesUseCase.executar();

		publicarEventosPendentesUseCase.executar();

		String requisicaoRecebida = REQUISICOES_RECEBIDAS.poll(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
		assertNotNull(requisicaoRecebida);
		assertTrue(requisicaoRecebida.contains(id.toString()));
		assertTrue(requisicaoRecebida.contains(TELEFONE_DO_FLUXO));
		assertTrue(requisicaoRecebida.contains("LEMBRETE_INICIAL"));

		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER + tokenValido()))
				.andExpect(jsonPath("$.status").value("PENDENTE"));

		mockMvc.perform(post("/alarmes/confirmacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"%s\"}".formatted(TELEFONE_DO_FLUXO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));

		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER + tokenValido()))
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));

		relogio.definir(INSTANTE_FIXO.plus(1, ChronoUnit.HOURS));
		escalonarAlarmesUseCase.executar();
		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, BEARER + tokenValido()))
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));
	}
}
