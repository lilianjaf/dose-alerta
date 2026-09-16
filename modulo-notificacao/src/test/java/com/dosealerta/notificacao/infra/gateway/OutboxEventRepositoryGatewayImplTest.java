package com.dosealerta.notificacao.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class OutboxEventRepositoryGatewayImplTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	private OutboxEvent eventoNovo() {
		return OutboxEvent.novo(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"+5511999999999",
				"Losartana",
				"50mg",
				EtapaEscalonamento.LEMBRETE_INICIAL,
				Canal.MENSAGEM,
				Instant.now());
	}

	@Test
	void deveSalvarEListarEventoPendente() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(eventoNovo());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10);

		assertTrue(pendentes.stream().anyMatch(e -> e.id().equals(salvo.id())));
	}

	@Test
	void deveMarcarEventoComoPublicadoERemoverDaListaDePendentes() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(eventoNovo());

		outboxEventRepositoryGateway.marcarComoPublicado(salvo.id(), Instant.now());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10);
		assertTrue(pendentes.stream().noneMatch(e -> e.id().equals(salvo.id())));
	}

	@Test
	void deveManterCamposAoSalvar() {
		OutboxEvent original = eventoNovo();

		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(original);

		assertEquals(original.alarmeId(), salvo.alarmeId());
		assertEquals(original.pacienteId(), salvo.pacienteId());
		assertEquals(original.telefone(), salvo.telefone());
		assertEquals(original.medicamento(), salvo.medicamento());
		assertEquals(original.dose(), salvo.dose());
		assertEquals(original.etapa(), salvo.etapa());
		assertEquals(original.canal(), salvo.canal());
		assertEquals(StatusOutboxEvent.PENDENTE, salvo.status());
	}
}
