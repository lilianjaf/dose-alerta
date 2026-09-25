package com.dosealerta.scheduler.carga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("carga")
@Testcontainers
@SpringBootTest(properties = "scheduler.escalonamento.intervalo-ms=3600000")
class EscalonamentoCargaTest {

	private static final int ALARMES = Integer.getInteger("carga.alarmes", 2_000);
	private static final long ORCAMENTO_SEGUNDOS = Long.getLong("carga.orcamento-segundos", 120);

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		var chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	@Autowired
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Autowired
	private EscalonarAlarmesUseCase escalonarAlarmesUseCase;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void umCicloDoJobEscalonaTodosOsAlarmesVencidosDentroDoOrcamento() {
		Instant agora = Instant.now();
		IntStream.range(0, ALARMES)
				.forEach(i -> alarmeRepositoryGateway.salvar(Alarme.criar(
						UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", agora.minusSeconds(60))));

		long inicio = System.nanoTime();
		escalonarAlarmesUseCase.executar(agora);
		Duration duracao = Duration.ofNanos(System.nanoTime() - inicio);

		System.out.printf(
				"[carga] escalonamento: %d alarmes em %d ms (%.0f alarmes/s)%n",
				ALARMES, duracao.toMillis(), ALARMES / Math.max(0.001, duracao.toMillis() / 1000.0));

		Integer escalonados = jdbcTemplate.queryForObject(
				"select count(*) from alarme where etapa_atual = ?", Integer.class, EtapaEscalonamento.LEMBRETE_INICIAL.name());
		assertEquals(ALARMES, escalonados, "todo alarme vencido deve avancar para o lembrete inicial");
		assertTrue(
				duracao.toSeconds() <= ORCAMENTO_SEGUNDOS,
				() -> "ciclo levou " + duracao.toSeconds() + "s, orcamento " + ORCAMENTO_SEGUNDOS + "s");
	}
}
