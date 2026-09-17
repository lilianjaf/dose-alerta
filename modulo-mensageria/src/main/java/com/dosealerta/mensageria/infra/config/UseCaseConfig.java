package com.dosealerta.mensageria.infra.config;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarRespostaMensagemUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase(AlarmeClientGateway alarmeClientGateway) {
		return new ProcessarRespostaMensagemUseCase(alarmeClientGateway);
	}

	@Bean
	public ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase(
			AlarmeClientGateway alarmeClientGateway) {
		return new ProcessarConfirmacaoLigacaoUseCase(alarmeClientGateway);
	}

	@Bean
	public ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase(AlarmeClientGateway alarmeClientGateway) {
		return new ProcessarStatusLigacaoUseCase(alarmeClientGateway);
	}
}
