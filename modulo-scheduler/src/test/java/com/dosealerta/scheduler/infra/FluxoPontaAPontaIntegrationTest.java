package com.dosealerta.scheduler.infra;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosPendentesUseCase;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

/**
 * Walking skeleton da Etapa 6: cobre, dentro da fronteira do modulo-scheduler, o ciclo
 * completo "alarme pendente -> escalonamento dispara a notificação -> paciente confirma".
 *
 * <p>O modulo-notificacao é substituído por um stub HTTP local (sem dependências extras de
 * teste) que apenas captura a requisição recebida, permitindo validar o contrato trocado
 * entre os dois módulos sem subir o módulo real. A confirmação do paciente é simulada
 * chamando diretamente o endpoint que o webhook do modulo-mensageria aciona em produção
 * (ver Etapa 6.2) — a entrega real da mensagem via Twilio Sandbox é o único passo que
 * permanece uma verificação manual (Etapa 3.1 ainda pendente de conta Twilio).
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class FluxoPontaAPontaIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	private static HttpServer stubNotificacao;
	private static final BlockingQueue<String> REQUISICOES_RECEBIDAS = new ArrayBlockingQueue<>(10);
	private static RSAPrivateKey chavePrivada;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));

		stubNotificacao = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		stubNotificacao.createContext("/notificacoes/solicitar-envio", exchange -> {
			try (exchange) {
				String corpo = new String(exchange.getRequestBody().readAllBytes());
				REQUISICOES_RECEBIDAS.offer(corpo);
				exchange.sendResponseHeaders(202, -1);
			}
		});
		stubNotificacao.start();
		registry.add("modulo-notificacao.uri", () -> "http://localhost:" + stubNotificacao.getAddress().getPort());
	}

	@AfterAll
	static void pararStub() {
		stubNotificacao.stop(0);
	}

	private static String tokenValido() throws Exception {
		Instant agora = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject("paciente-1")
				.issueTime(Date.from(agora))
				.expirationTime(Date.from(agora.plusSeconds(3600)))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chavePrivada));
		return signedJWT.serialize();
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EscalonarAlarmesUseCase escalonarAlarmesUseCase;

	@Autowired
	private PublicarEventosPendentesUseCase publicarEventosPendentesUseCase;

	@Test
	void deveEscalonarNotificarEConfirmarUmAlarmeDePontaAPonta() throws Exception {
		String telefone = "+5511977776666";
		var criacao = new CriarAlarmeInput(
				UUID.randomUUID(), telefone, "Losartana", "50mg", Instant.now().minus(1, ChronoUnit.MINUTES));

		String resposta = mockMvc.perform(post("/alarmes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(criacao)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		UUID id = UUID.fromString(objectMapper.readTree(resposta).get("id").asText());

		// dono do relógio: decide que já passou da hora e grava o evento de outbox
		escalonarAlarmesUseCase.executar(Instant.now());
		// publisher assíncrono do outbox: aciona o modulo-notificacao (aqui, o stub)
		publicarEventosPendentesUseCase.executar();

		String requisicaoRecebida = REQUISICOES_RECEBIDAS.poll(5, TimeUnit.SECONDS);
		assertNotNull(requisicaoRecebida);
		assertTrue(requisicaoRecebida.contains(id.toString()));
		assertTrue(requisicaoRecebida.contains(telefone));
		assertTrue(requisicaoRecebida.contains("LEMBRETE_INICIAL"));

		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(jsonPath("$.status").value("PENDENTE"));

		// simula o que o webhook do modulo-mensageria aciona ao receber a resposta do paciente
		mockMvc.perform(post("/alarmes/confirmacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"%s\"}".formatted(telefone)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));

		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));

		// alarme confirmado não deve mais ser escalonado
		escalonarAlarmesUseCase.executar(Instant.now().plus(1, ChronoUnit.HOURS));
		mockMvc.perform(get("/alarmes/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(jsonPath("$.status").value("CONFIRMADO"));
	}
}
