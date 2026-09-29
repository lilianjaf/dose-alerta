package com.dosealerta.ia.infra.config;

import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import com.dosealerta.ia.core.usecase.InterpretarAudioUseCase;
import com.dosealerta.ia.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.ia.infra.client.ExtratorReceitaGatewayComMockDeEmergencia;
import com.dosealerta.ia.infra.client.MockExtratorReceitaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public ExtrairReceitaUseCase extrairReceitaUseCase(
			ExtratorReceitaGateway extratorReceitaGateway,
			ReceitaRepositoryGateway receitaRepositoryGateway,
			@Value("${ia.mock-de-emergencia-habilitado:false}") boolean mockDeEmergenciaHabilitado) {
		ExtratorReceitaGateway gateway = mockDeEmergenciaHabilitado
				? new ExtratorReceitaGatewayComMockDeEmergencia(extratorReceitaGateway)
				: extratorReceitaGateway;
		return new ExtrairReceitaUseCase(gateway, receitaRepositoryGateway);
	}

	@Bean
	public ExtrairReceitaUseCase extrairReceitaUseCaseMock(ReceitaRepositoryGateway receitaRepositoryGateway) {
		return new ExtrairReceitaUseCase(new MockExtratorReceitaGateway(), receitaRepositoryGateway);
	}

	@Bean
	public ConfirmarReceitaUseCase confirmarReceitaUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway) {
		return new ConfirmarReceitaUseCase(receitaRepositoryGateway, feedbackExtracaoRepositoryGateway);
	}

	@Bean
	public ConfirmarReceitaPorTelefoneUseCase confirmarReceitaPorTelefoneUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway, ConfirmarReceitaUseCase confirmarReceitaUseCase) {
		return new ConfirmarReceitaPorTelefoneUseCase(receitaRepositoryGateway, confirmarReceitaUseCase);
	}

	@Bean
	public BuscarReceitaUseCase buscarReceitaUseCase(ReceitaRepositoryGateway receitaRepositoryGateway) {
		return new BuscarReceitaUseCase(receitaRepositoryGateway);
	}

	@Bean
	public InterpretarAudioUseCase interpretarAudioUseCase(InterpretadorAudioGateway interpretadorAudioGateway) {
		return new InterpretarAudioUseCase(interpretadorAudioGateway);
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
