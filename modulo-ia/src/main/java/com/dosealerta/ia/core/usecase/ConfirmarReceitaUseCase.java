package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.FeedbackExtracao;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import java.time.Instant;
import java.util.UUID;

public class ConfirmarReceitaUseCase {

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway;

	public ConfirmarReceitaUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.feedbackExtracaoRepositoryGateway = feedbackExtracaoRepositoryGateway;
	}

	public Receita executar(UUID receitaId, ConfirmarReceitaInput input) {
		Receita receita = receitaRepositoryGateway
				.buscarPorId(receitaId)
				.orElseThrow(() -> new ReceitaNaoEncontradaException(receitaId));

		boolean corrigido =
				receita.difereDe(input.medicamento(), input.dose(), input.frequenciaHoras(), input.duracaoDias());
		Instant agora = Instant.now();
		FeedbackExtracao feedback = FeedbackExtracao.registrar(
				receita, input.medicamento(), input.dose(), input.frequenciaHoras(), input.duracaoDias(), corrigido, agora);
		feedbackExtracaoRepositoryGateway.salvar(feedback);

		receita.confirmar(input.medicamento(), input.dose(), input.frequenciaHoras(), input.duracaoDias(), agora);
		return receitaRepositoryGateway.salvar(receita);
	}
}
