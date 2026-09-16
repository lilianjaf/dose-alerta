package com.dosealerta.scheduler.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.time.Instant;
import java.util.List;
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
class AlarmeRepositoryGatewayImplTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Autowired
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Test
	void deveManterEventosDeOutboxAnterioresAoRegistrarNovoEnvio() {
		Instant horarioAlvo = Instant.now();
		Alarme alarme = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", horarioAlvo));

		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, horarioAlvo);
		alarme = alarmeRepositoryGateway.salvar(alarme);

		Alarme recarregado = alarmeRepositoryGateway.buscarPorId(alarme.getId()).orElseThrow();
		assertEquals(EtapaEscalonamento.LEMBRETE_INICIAL, recarregado.getEtapaAtual());
		assertEquals(1, recarregado.getEventosOutbox().size());
		assertEquals(StatusOutboxEvent.PENDENTE, recarregado.getEventosOutbox().get(0).status());

		recarregado.registrarEnvio(EtapaEscalonamento.REFORCO, horarioAlvo.plusSeconds(900));
		alarmeRepositoryGateway.salvar(recarregado);

		Alarme aposSegundoEnvio =
				alarmeRepositoryGateway.buscarPorId(alarme.getId()).orElseThrow();
		assertEquals(EtapaEscalonamento.REFORCO, aposSegundoEnvio.getEtapaAtual());
		assertEquals(2, aposSegundoEnvio.getEventosOutbox().size());
	}

	@Test
	void devePersistirInteracaoAoConfirmarAlarme() {
		Alarme alarme = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));

		alarme.confirmar(Instant.now());
		alarmeRepositoryGateway.salvar(alarme);

		Alarme recarregado = alarmeRepositoryGateway.buscarPorId(alarme.getId()).orElseThrow();
		assertEquals(StatusAlarme.CONFIRMADO, recarregado.getStatus());
		assertEquals(1, recarregado.getInteracoes().size());
		assertEquals(TipoInteracao.CONFIRMACAO, recarregado.getInteracoes().get(0).tipo());
	}

	@Test
	void deveListarApenasAlarmesPendentes() {
		Alarme pendente = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));
		Alarme confirmado = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Metformina", "850mg", Instant.now()));
		confirmado.confirmar(Instant.now());
		alarmeRepositoryGateway.salvar(confirmado);

		List<Alarme> pendentes = alarmeRepositoryGateway.buscarPendentesParaEscalonamento();

		assertTrue(pendentes.stream().anyMatch(a -> a.getId().equals(pendente.getId())));
		assertTrue(pendentes.stream().noneMatch(a -> a.getId().equals(confirmado.getId())));
	}

	@Test
	void deveMarcarEventoComoPublicadoERemoverDaListaDePendentes() {
		Alarme alarme = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, Instant.now());
		alarmeRepositoryGateway.salvar(alarme);

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10);
		var evento = pendentes.stream()
				.filter(e -> e.alarmeId().equals(alarme.getId()))
				.findFirst()
				.orElseThrow();

		outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());

		var pendentesDepois = outboxEventRepositoryGateway.buscarPendentes(10);
		assertTrue(pendentesDepois.stream().noneMatch(e -> e.id().equals(evento.id())));
	}
}
