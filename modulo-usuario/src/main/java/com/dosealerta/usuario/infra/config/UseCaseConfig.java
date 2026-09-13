package com.dosealerta.usuario.infra.config;

import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import com.dosealerta.usuario.core.usecase.AutenticarPacienteUseCase;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public CadastrarPacienteUseCase cadastrarPacienteUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway, SenhaGateway senhaGateway) {
		return new CadastrarPacienteUseCase(pacienteRepositoryGateway, senhaGateway);
	}

	@Bean
	public AutenticarPacienteUseCase autenticarPacienteUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway,
			SenhaGateway senhaGateway,
			AutenticacaoGateway autenticacaoGateway) {
		return new AutenticarPacienteUseCase(pacienteRepositoryGateway, senhaGateway, autenticacaoGateway);
	}
}
