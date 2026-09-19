package com.dosealerta.relatorioadesao.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
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
class InteracaoRepositoryGatewayImplTest {

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
	private InteracaoRepositoryGateway interacaoRepositoryGateway;

	@Test
	void deveSalvarEBuscarInteracoesDoPacienteNoPeriodo() {
		UUID pacienteId = UUID.randomUUID();
		Instant agora = Instant.now();
		interacaoRepositoryGateway.salvar(
				new Interacao(UUID.randomUUID(), pacienteId, "Losartana", TipoInteracao.CONFIRMACAO, agora));
		interacaoRepositoryGateway.salvar(
				new Interacao(UUID.randomUUID(), UUID.randomUUID(), "Losartana", TipoInteracao.CONFIRMACAO, agora));

		List<Interacao> encontradas = interacaoRepositoryGateway.buscarPorPacienteEPeriodo(
				pacienteId, agora.minusSeconds(60), agora.plusSeconds(60));

		assertEquals(1, encontradas.size());
		assertTrue(encontradas.stream().allMatch(i -> i.pacienteId().equals(pacienteId)));
	}

	@Test
	void deveSerIdempotenteAoSalvarAMesmaInteracaoDuasVezes() {
		UUID id = UUID.randomUUID();
		UUID pacienteId = UUID.randomUUID();
		Instant agora = Instant.now();
		Interacao interacao = new Interacao(id, pacienteId, "Losartana", TipoInteracao.CONFIRMACAO, agora);

		interacaoRepositoryGateway.salvar(interacao);
		interacaoRepositoryGateway.salvar(interacao);

		List<Interacao> encontradas = interacaoRepositoryGateway.buscarPorPacienteEPeriodo(
				pacienteId, agora.minusSeconds(60), agora.plusSeconds(60));
		assertEquals(1, encontradas.size());
	}
}
