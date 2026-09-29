package com.dosealerta.ia.infra.gateway;

import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.dosealerta.ia.TesteIntegracaoBase;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class ConfirmarReceitaTransacaoTest extends TesteIntegracaoBase {

	private static final String CORPO_CONFIRMACAO =
			"{\"medicamento\":\"Losartana\",\"dose\":\"50mg\",\"frequenciaHoras\":24,\"duracaoDias\":30}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FeedbackExtracaoJpaRepository feedbackExtracaoJpaRepository;

	@MockitoBean
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Test
	void naoDeveDeixarFeedbackOrfaoQuandoSalvarAReceitaFalhaAposSalvarOFeedback() throws Exception {
		Receita receita = umaReceita();
		when(receitaRepositoryGateway.buscarPorId(RECEITA_ID)).thenReturn(Optional.of(receita));
		when(receitaRepositoryGateway.salvar(any(Receita.class)))
				.thenThrow(new RuntimeException("falha simulada de infraestrutura"));

		mockMvc.perform(post("/receitas/{id}/confirmar", RECEITA_ID)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
				.contentType(MediaType.APPLICATION_JSON)
				.content(CORPO_CONFIRMACAO));

		assertEquals(0, feedbackExtracaoJpaRepository.count());
	}
}
