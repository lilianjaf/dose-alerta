package com.dosealerta.notificacao.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
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

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10, Instant.now());

		assertTrue(pendentes.stream().anyMatch(e -> e.id().equals(salvo.id())));
	}

	@Test
	void deveMarcarEventoComoPublicadoERemoverDaListaDePendentes() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(eventoNovo());

		outboxEventRepositoryGateway.marcarComoPublicado(salvo.id(), Instant.now());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10, Instant.now());
		assertTrue(pendentes.stream().noneMatch(e -> e.id().equals(salvo.id())));
	}

	@Test
	void deveAdiarOEventoAteAProximaTentativaERetomarQuandoVencer() {
		OutboxEvent salvo = outboxEventRepositoryGateway.salvar(eventoNovo());
		Instant agora = Instant.now();

		outboxEventRepositoryGateway.registrarFalha(salvo.id(), 1, agora.plusSeconds(60));

		assertTrue(outboxEventRepositoryGateway.buscarPendentes(50, agora).stream()
				.noneMatch(e -> e.id().equals(salvo.id())));
		var vencido = outboxEventRepositoryGateway.buscarPendentes(50, agora.plusSeconds(61)).stream()
				.filter(e -> e.id().equals(salvo.id()))
				.findFirst()
				.orElseThrow();
		assertEquals(1, vencido.tentativas());
	}

	@Test
	void deveTirarDaFilaOEventoQueFalhouOuExpirou() {
		OutboxEvent falhou = outboxEventRepositoryGateway.salvar(eventoNovo());
		OutboxEvent expirado = outboxEventRepositoryGateway.salvar(eventoNovo());

		outboxEventRepositoryGateway.marcarComoFalhou(falhou.id(), 5);
		outboxEventRepositoryGateway.marcarComoExpirado(expirado.id());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(50, Instant.now().plusSeconds(3600));
		assertTrue(pendentes.stream().noneMatch(e -> e.id().equals(falhou.id()) || e.id().equals(expirado.id())));
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
