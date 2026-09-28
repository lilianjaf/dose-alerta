package com.dosealerta.ia.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dosealerta.ia.core.domain.FeedbackExtracao;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusOutboxEvent;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
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
class ReceitaRepositoryGatewayImplTest {

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
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Autowired
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Autowired
	private FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;

	@Test
	void deveGravarOOutboxEventAoConfirmarNaMesmaTransacaoDaReceita() {
		Receita receita = receitaRepositoryGateway.salvar(Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, Instant.now()));

		receita.confirmar("Losartana", "50mg", 24, 30, Instant.now());
		receitaRepositoryGateway.salvar(receita);

		Receita recarregada = receitaRepositoryGateway.buscarPorId(receita.getId()).orElseThrow();
		assertEquals(StatusReceita.CONFIRMADA, recarregada.getStatus());
		assertEquals(1, recarregada.getEventosOutbox().size());
		assertEquals(StatusOutboxEvent.PENDENTE, recarregada.getEventosOutbox().get(0).status());

		var pendentes = outboxEventRepositoryGateway.buscarPendentes(10);
		var evento = pendentes.stream()
				.filter(e -> e.receitaId().equals(receita.getId()))
				.findFirst()
				.orElseThrow();

		outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());

		var pendentesDepois = outboxEventRepositoryGateway.buscarPendentes(10);
		assertEquals(0, pendentesDepois.stream().filter(e -> e.id().equals(evento.id())).count());
	}

	@Test
	void deveBuscarAMaisRecenteAguardandoConfirmacaoPorTelefoneIgnorandoAsJaConfirmadas() {
		String telefone = "+5511988887777";
		Receita antiga = receitaRepositoryGateway.salvar(Receita.aguardandoConfirmacao(
				UUID.randomUUID(), telefone, "Amoxicilina", "500mg", 8, 7, Instant.now()));
		antiga.confirmar("Amoxicilina", "500mg", 8, 7, Instant.now());
		receitaRepositoryGateway.salvar(antiga);

		Receita pendente = receitaRepositoryGateway.salvar(Receita.aguardandoConfirmacao(
				UUID.randomUUID(), telefone, "Losartana", "50mg", 24, 30, Instant.now()));

		Receita encontrada = receitaRepositoryGateway
				.buscarAguardandoConfirmacaoMaisRecentePorTelefone(telefone)
				.orElseThrow();

		assertEquals(pendente.getId(), encontrada.getId());
	}

	@Test
	void deveSalvarFeedbackDeExtracaoDeFormaIndependente() {
		Receita receita = receitaRepositoryGateway.salvar(Receita.aguardandoConfirmacao(
				UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", 24, 30, Instant.now()));

		FeedbackExtracao feedback = FeedbackExtracao.registrar(receita, "Losartana", "100mg", 24, 30, true, Instant.now());
		feedbackExtracaoRepositoryGateway.salvar(feedback);
	}
}
