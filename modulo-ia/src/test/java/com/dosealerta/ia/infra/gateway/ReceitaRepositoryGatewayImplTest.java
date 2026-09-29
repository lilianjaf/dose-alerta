package com.dosealerta.ia.infra.gateway;

import static com.dosealerta.ia.IaFixtures.CORRELATION_ID;
import static com.dosealerta.ia.IaFixtures.DOSE;
import static com.dosealerta.ia.IaFixtures.DURACAO_DIAS;
import static com.dosealerta.ia.IaFixtures.FREQUENCIA_HORAS;
import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.MEDICAMENTO;
import static com.dosealerta.ia.IaFixtures.TELEFONE;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static com.dosealerta.ia.IaFixtures.umaReceitaCom;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dosealerta.ia.TesteIntegracaoBase;
import com.dosealerta.ia.core.domain.FeedbackExtracao;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusOutboxEvent;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReceitaRepositoryGatewayImplTest extends TesteIntegracaoBase {


	@Autowired
	private ReceitaRepositoryGateway receitaRepositoryGateway;

	@Autowired
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Autowired
	private FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;

	private Receita salvarConfirmada(Receita receita) {
		Receita salva = receitaRepositoryGateway.salvar(receita);
		salva.confirmar(
				salva.getMedicamento(), salva.getDose(), FREQUENCIA_HORAS, DURACAO_DIAS, INSTANTE_FIXO, CORRELATION_ID);
		return receitaRepositoryGateway.salvar(salva);
	}

	@Test
	void deveGravarOOutboxEventAoConfirmarNaMesmaTransacaoDaReceita() {
		Receita receita = salvarConfirmada(umaReceita());

		Receita recarregada = receitaRepositoryGateway.buscarPorId(receita.getId()).orElseThrow();
		assertEquals(StatusReceita.CONFIRMADA, recarregada.getStatus());
		assertEquals(1, recarregada.getEventosOutbox().size());
		assertEquals(StatusOutboxEvent.PENDENTE, recarregada.getEventosOutbox().get(0).status());

		var evento = outboxEventRepositoryGateway.buscarPendentes(10).stream()
				.filter(e -> e.receitaId().equals(receita.getId()))
				.findFirst()
				.orElseThrow();
		outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), INSTANTE_FIXO);

		var pendentesDepois = outboxEventRepositoryGateway.buscarPendentes(10);
		assertEquals(0, pendentesDepois.stream().filter(e -> e.id().equals(evento.id())).count());
	}

	@Test
	void deveBuscarAMaisRecenteAguardandoConfirmacaoPorTelefoneIgnorandoAsJaConfirmadas() {
		salvarConfirmada(umaReceitaCom("Amoxicilina", "500mg", 8, 7));
		Receita pendente = receitaRepositoryGateway.salvar(umaReceitaCom(MEDICAMENTO, DOSE, FREQUENCIA_HORAS, DURACAO_DIAS));

		Receita encontrada = receitaRepositoryGateway
				.buscarAguardandoConfirmacaoMaisRecentePorTelefone(TELEFONE)
				.orElseThrow();

		assertEquals(pendente.getId(), encontrada.getId());
	}

	@Test
	void deveSalvarFeedbackDeExtracaoDeFormaIndependente() {
		Receita receita = receitaRepositoryGateway.salvar(umaReceita());
		FeedbackExtracao feedback = FeedbackExtracao.registrar(
				receita, MEDICAMENTO, "100mg", FREQUENCIA_HORAS, DURACAO_DIAS, true, INSTANTE_FIXO);

		feedbackExtracaoRepositoryGateway.salvar(feedback);
	}
}
