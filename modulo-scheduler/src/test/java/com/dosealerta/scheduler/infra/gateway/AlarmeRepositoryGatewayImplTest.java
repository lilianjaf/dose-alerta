package com.dosealerta.scheduler.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
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
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Autowired
	private EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway;

	@Test
	void deveBuscarAlarmePendenteDoMesmoMedicamentoIgnorandoMaiusculas() {
		UUID pacienteId = UUID.randomUUID();
		Alarme salvo = alarmeRepositoryGateway.salvar(
				Alarme.criar(pacienteId, "+5511999999999", "Aerolin Spray", "2 doses", Instant.now()));

		assertEquals(
				salvo.getId(),
				alarmeRepositoryGateway
						.buscarPendentePorPacienteEMedicamento(pacienteId, "aerolin spray")
						.orElseThrow()
						.getId());
		assertTrue(alarmeRepositoryGateway
				.buscarPendentePorPacienteEMedicamento(pacienteId, "Losartana")
				.isEmpty());
		assertTrue(alarmeRepositoryGateway
				.buscarPendentePorPacienteEMedicamento(UUID.randomUUID(), "Aerolin Spray")
				.isEmpty());
	}

	@Test
	void naoDeveConsiderarDuplicataQuandoOAlarmeDoMedicamentoJaFoiConfirmado() {
		UUID pacienteId = UUID.randomUUID();
		Instant horarioAlvo = Instant.now();
		Alarme alarme = Alarme.criar(pacienteId, "+5511999999999", "Losartana", "50mg", horarioAlvo);
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, horarioAlvo);
		alarme.confirmar(horarioAlvo.plusSeconds(60));
		alarmeRepositoryGateway.salvar(alarme);

		assertTrue(alarmeRepositoryGateway
				.buscarPendentePorPacienteEMedicamento(pacienteId, "Losartana")
				.isEmpty());
	}

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
	void deveGravarEventoDeInteracaoNoOutboxAoConfirmarAlarme() {
		Alarme alarme = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));

		alarme.confirmar(Instant.now());
		alarmeRepositoryGateway.salvar(alarme);

		var pendentes = eventoInteracaoRepositoryGateway.buscarPendentes(10);
		var evento = pendentes.stream()
				.filter(e -> e.alarmeId().equals(alarme.getId()))
				.findFirst()
				.orElseThrow();
		assertEquals(alarme.getPacienteId(), evento.pacienteId());
		assertEquals("Losartana", evento.medicamento());
		assertEquals(TipoInteracao.CONFIRMACAO, evento.tipo());
		assertEquals(StatusOutboxEvent.PENDENTE, evento.status());

		eventoInteracaoRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());

		var pendentesDepois = eventoInteracaoRepositoryGateway.buscarPendentes(10);
		assertTrue(pendentesDepois.stream().noneMatch(e -> e.id().equals(evento.id())));
	}

	@Test
	void deveGravarEventoDeInteracaoNoOutboxAoMarcarAlarmeComoNaoConfirmado() {
		Alarme alarme = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));

		alarme.marcarNaoConfirmado(Instant.now());
		alarmeRepositoryGateway.salvar(alarme);

		var evento = eventoInteracaoRepositoryGateway.buscarPendentes(10).stream()
				.filter(e -> e.alarmeId().equals(alarme.getId()))
				.findFirst()
				.orElseThrow();
		assertEquals(alarme.getPacienteId(), evento.pacienteId());
		assertEquals("Losartana", evento.medicamento());
		assertEquals(TipoInteracao.NAO_CONFIRMACAO, evento.tipo());
	}

	@Test
	void deveGravarEventoDeInteracaoNoOutboxAoRegistrarLigacaoAtendida() {
		Alarme alarme = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));

		alarme.registrarLigacaoAtendida(Instant.now());
		alarmeRepositoryGateway.salvar(alarme);

		var evento = eventoInteracaoRepositoryGateway.buscarPendentes(10).stream()
				.filter(e -> e.alarmeId().equals(alarme.getId()))
				.findFirst()
				.orElseThrow();
		assertEquals(alarme.getPacienteId(), evento.pacienteId());
		assertEquals("Losartana", evento.medicamento());
		assertEquals(TipoInteracao.LIGACAO_ATENDIDA, evento.tipo());
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

	@Test
	void deveLancarConflitoDeConcorrenciaQuandoDuasCopiasDoMesmoAlarmeSaoSalvas() {
		Alarme original = alarmeRepositoryGateway.salvar(
				Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now()));

		Alarme copiaA = alarmeRepositoryGateway.buscarPorId(original.getId()).orElseThrow();
		Alarme copiaB = alarmeRepositoryGateway.buscarPorId(original.getId()).orElseThrow();

		copiaA.confirmar(Instant.now());
		alarmeRepositoryGateway.salvar(copiaA);

		copiaB.registrarLigacaoAtendida(Instant.now());
		assertThrows(ConflitoConcorrenciaException.class, () -> alarmeRepositoryGateway.salvar(copiaB));

		Alarme recarregado = alarmeRepositoryGateway.buscarPorId(original.getId()).orElseThrow();
		assertEquals(StatusAlarme.CONFIRMADO, recarregado.getStatus());
		assertEquals(1, recarregado.getInteracoes().size());
	}
}
