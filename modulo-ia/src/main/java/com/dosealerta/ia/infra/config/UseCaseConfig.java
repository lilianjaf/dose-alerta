package com.dosealerta.ia.infra.config;

import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import com.dosealerta.ia.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.ia.infra.client.ExtratorReceitaGatewayComMockDeEmergencia;
import com.dosealerta.ia.infra.client.MockExtratorReceitaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	/**
	 * {@code ia.mock-de-emergencia-habilitado}: rede de segurança pra demonstração (ex: gravando vídeo do
	 * celular) — se o Gemini falhar mesmo depois do fallback entre modelos, devolve dados fixos em vez de
	 * quebrar a conversa no WhatsApp. Desligado por padrão: mascara uma falha real do modelo de visão, então só
	 * vale a pena ligar sabendo disso (ver {@code ExtratorReceitaGatewayComMockDeEmergencia}).
	 */
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

	/**
	 * Mesma extração, mas com dados fixos em vez de chamar o Gemini — pra testar confirmação e criação de alarme
	 * sem depender do modelo de visão estar no ar (ver {@code /receitas/extrair-mock} em {@code ReceitaController}).
	 */
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
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			SchedulerClientGateway schedulerClientGateway) {
		return new PublicarEventosPendentesUseCase(
				receitaRepositoryGateway, outboxEventRepositoryGateway, schedulerClientGateway);
	}
}
