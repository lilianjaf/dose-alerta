package com.dosealerta.ia.infra.config;

import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import com.dosealerta.ia.core.usecase.PublicarEventosPendentesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public ExtrairReceitaUseCase extrairReceitaUseCase(
			ExtratorReceitaGateway extratorReceitaGateway, ReceitaRepositoryGateway receitaRepositoryGateway) {
		return new ExtrairReceitaUseCase(extratorReceitaGateway, receitaRepositoryGateway);
	}

	@Bean
	public ConfirmarReceitaUseCase confirmarReceitaUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway) {
		return new ConfirmarReceitaUseCase(receitaRepositoryGateway, feedbackExtracaoRepositoryGateway);
	}

	@Bean
	public BuscarReceitaUseCase buscarReceitaUseCase(ReceitaRepositoryGateway receitaRepositoryGateway) {
		return new BuscarReceitaUseCase(receitaRepositoryGateway);
	}

	@Bean
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			SchedulerClientGateway schedulerClientGateway) {
		return new PublicarEventosPendentesUseCase(
				receitaRepositoryGateway, outboxEventRepositoryGateway, schedulerClientGateway);
	}
}
