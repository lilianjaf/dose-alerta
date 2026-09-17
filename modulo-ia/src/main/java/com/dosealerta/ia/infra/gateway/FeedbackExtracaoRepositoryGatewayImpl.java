package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.core.domain.FeedbackExtracao;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.infra.gateway.entity.FeedbackExtracaoJpaEntity;
import org.springframework.stereotype.Component;

@Component
class FeedbackExtracaoRepositoryGatewayImpl implements FeedbackExtracaoRepositoryGateway {

	private final FeedbackExtracaoJpaRepository feedbackExtracaoJpaRepository;

	FeedbackExtracaoRepositoryGatewayImpl(FeedbackExtracaoJpaRepository feedbackExtracaoJpaRepository) {
		this.feedbackExtracaoJpaRepository = feedbackExtracaoJpaRepository;
	}

	@Override
	public void salvar(FeedbackExtracao feedback) {
		feedbackExtracaoJpaRepository.save(new FeedbackExtracaoJpaEntity(
				feedback.id(),
				feedback.receitaId(),
				feedback.medicamentoExtraido(),
				feedback.doseExtraida(),
				feedback.frequenciaExtraidaHoras(),
				feedback.duracaoExtraidaDias(),
				feedback.medicamentoConfirmado(),
				feedback.doseConfirmada(),
				feedback.frequenciaConfirmadaHoras(),
				feedback.duracaoConfirmadaDias(),
				feedback.corrigido(),
				feedback.registradoEm()));
	}
}
