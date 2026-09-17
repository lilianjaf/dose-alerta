package com.dosealerta.ia.infra.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prova que {@code ReceitaController.confirmar} é transacional o suficiente para não deixar
 * um {@code FeedbackExtracao} órfão quando a gravação da {@code Receita} falha depois —
 * ver achado de code review da Etapa 7 (as duas gravações do usecase não tinham limite
 * transacional em comum antes desta correção).
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ConfirmarReceitaTransacaoTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FeedbackExtracaoJpaRepository feedbackExtracaoJpaRepository;

	@MockitoBean
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Test
	void naoDeveDeixarFeedbackOrfaoQuandoSalvarAReceitaFalhaAposSalvarOFeedback() throws Exception {
		UUID receitaId = UUID.randomUUID();
		Receita receita = Receita.existente(
				receitaId,
				UUID.randomUUID(),
				"+5511999999999",
				"Losartana",
				"50mg",
				24,
				30,
				Instant.now(),
				Instant.now(),
				StatusReceita.AGUARDANDO_CONFIRMACAO,
				List.of());
		when(receitaRepositoryGateway.buscarPorId(receitaId)).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class)))
				.thenThrow(new RuntimeException("falha simulada de infraestrutura"));

		// A falha simulada no segundo salvar() propaga como exceção não tratada (não há
		// @ExceptionHandler para RuntimeException genérica) — o que importa aqui não é o
		// status HTTP resultante, e sim que a transação tenha revertido o insert do feedback.
		assertThrows(Exception.class, () -> mockMvc.perform(post("/receitas/{id}/confirmar", receitaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"medicamento\":\"Losartana\",\"dose\":\"50mg\",\"frequenciaHoras\":24,\"duracaoDias\":30}")));

		assertEquals(0, feedbackExtracaoJpaRepository.count());
	}
}
