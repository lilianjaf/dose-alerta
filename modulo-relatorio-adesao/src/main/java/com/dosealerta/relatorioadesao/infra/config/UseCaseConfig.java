package com.dosealerta.relatorioadesao.infra.config;

import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.usecase.ConsultarTaxaAdesaoUseCase;
import com.dosealerta.relatorioadesao.core.usecase.RegistrarInteracaoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public RegistrarInteracaoUseCase registrarInteracaoUseCase(InteracaoRepositoryGateway interacaoRepositoryGateway) {
		return new RegistrarInteracaoUseCase(interacaoRepositoryGateway);
	}

	@Bean
	public ConsultarTaxaAdesaoUseCase consultarTaxaAdesaoUseCase(InteracaoRepositoryGateway interacaoRepositoryGateway) {
		return new ConsultarTaxaAdesaoUseCase(interacaoRepositoryGateway);
	}
}
