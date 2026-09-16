package com.dosealerta.notificacao.infra.config;

import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.notificacao.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.notificacao.core.usecase.SolicitarEnvioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public SolicitarEnvioUseCase solicitarEnvioUseCase(
			EstrategiaCanalGateway estrategiaCanalGateway, OutboxEventRepositoryGateway outboxEventRepositoryGateway) {
		return new SolicitarEnvioUseCase(estrategiaCanalGateway, outboxEventRepositoryGateway);
	}

	@Bean
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			OutboxEventRepositoryGateway outboxEventRepositoryGateway, MensageriaClientGateway mensageriaClientGateway) {
		return new PublicarEventosPendentesUseCase(outboxEventRepositoryGateway, mensageriaClientGateway);
	}
}
