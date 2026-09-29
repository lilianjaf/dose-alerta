package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.FeedbackExtracao;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.gateway.CorrelacaoGateway;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.TransactionGateway;
import com.dosealerta.ia.core.rules.confirmar.ConfirmacaoReceitaContext;
import com.dosealerta.ia.core.rules.confirmar.ValidadorConfirmacaoReceitaRule;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ConfirmarReceitaUseCaseImpl implements ConfirmarReceitaUseCase {

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;
	private final TransactionGateway transactionGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;
	private final List<ValidadorConfirmacaoReceitaRule> rules;

	public ConfirmarReceitaUseCaseImpl(ReceitaRepositoryGateway receitaRepositoryGateway, FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway, TransactionGateway transactionGateway, CorrelacaoGateway correlacaoGateway, Clock clock, List<ValidadorConfirmacaoReceitaRule> rules) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.feedbackExtracaoRepositoryGateway = feedbackExtracaoRepositoryGateway;
		this.transactionGateway = transactionGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public Receita executar(UUID receitaId, ConfirmarReceitaInput input) {
		Receita receita = receitaId == null ? null : receitaRepositoryGateway.buscarPorId(receitaId).orElse(null);
		ConfirmacaoReceitaContext context = criarContexto(receitaId, receita, input);
		rules.forEach(rule -> rule.validar(context));

		return transactionGateway.execute(() -> confirmar(receita, context));
	}

	private ConfirmacaoReceitaContext criarContexto(UUID receitaId, Receita receita, ConfirmarReceitaInput input) {
		if (receita == null) {
			return new ConfirmacaoReceitaContext(
					receitaId, null, input.medicamento(), input.dose(), input.frequenciaHoras(), input.duracaoDias());
		}
		return new ConfirmacaoReceitaContext(
				receitaId,
				receita,
				mesclar(input.medicamento(), receita.getMedicamento()),
				mesclar(input.dose(), receita.getDose()),
				mesclar(input.frequenciaHoras(), receita.getFrequenciaHoras()),
				mesclar(input.duracaoDias(), receita.getDuracaoDias()));
	}

	private <T> T mesclar(T informado, T atual) {
		return informado != null ? informado : atual;
	}

	private Receita confirmar(Receita receita, ConfirmacaoReceitaContext context) {
		Instant agora = clock.instant();
		boolean corrigido = receita.difereDe(
				context.medicamento(), context.dose(), context.frequenciaHoras(), context.duracaoDias());
		FeedbackExtracao feedback = FeedbackExtracao.registrar(
				receita,
				context.medicamento(),
				context.dose(),
				context.frequenciaHoras(),
				context.duracaoDias(),
				corrigido,
				agora);
		feedbackExtracaoRepositoryGateway.salvar(feedback);

		receita.confirmar(
				context.medicamento(),
				context.dose(),
				context.frequenciaHoras(),
				context.duracaoDias(),
				agora,
				correlacaoGateway.atual());
		return receitaRepositoryGateway.salvar(receita);
	}
}
