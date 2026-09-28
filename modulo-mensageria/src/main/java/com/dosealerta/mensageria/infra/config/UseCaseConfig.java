package com.dosealerta.mensageria.infra.config;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.MediaDownloadGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import com.dosealerta.mensageria.core.gateway.PacienteClientGateway;
import com.dosealerta.mensageria.core.gateway.ReceitaClientGateway;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
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
	public ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase(
			PacienteClientGateway pacienteClientGateway,
			ReceitaClientGateway receitaClientGateway,
			MediaDownloadGateway mediaDownloadGateway,
			MensageriaGateway mensageriaGateway,
			ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase) {
		return new ProcessarMensagemRecebidaUseCase(
				pacienteClientGateway,
				receitaClientGateway,
				mediaDownloadGateway,
				mensageriaGateway,
				processarRespostaMensagemUseCase);
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
