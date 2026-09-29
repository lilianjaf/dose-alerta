package com.dosealerta.scheduler.infra.config;

import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosInteracaoPendentesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarConfirmacaoUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarLigacaoAtendidaUseCase;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

	@Bean
	public CriarAlarmeUseCase criarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		return new CriarAlarmeUseCase(alarmeRepositoryGateway);
	}

	@Bean
	public BuscarAlarmeUseCase buscarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		return new BuscarAlarmeUseCase(alarmeRepositoryGateway);
	}

	@Bean
	public RegistrarConfirmacaoUseCase registrarConfirmacaoUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway, MetricasAlarmeGateway metricasAlarmeGateway) {
		return new RegistrarConfirmacaoUseCase(alarmeRepositoryGateway, metricasAlarmeGateway);
	}

	@Bean
	public RegistrarLigacaoAtendidaUseCase registrarLigacaoAtendidaUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway) {
		return new RegistrarLigacaoAtendidaUseCase(alarmeRepositoryGateway);
	}

	@Bean
	public EscalonarAlarmesUseCase escalonarAlarmesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			MetricasAlarmeGateway metricasAlarmeGateway,
			@Value("${scheduler.escalonamento.intervalo-entre-etapas-ms:900000}") long intervaloEntreEtapasMs) {
		return new EscalonarAlarmesUseCase(
				alarmeRepositoryGateway, metricasAlarmeGateway, Duration.ofMillis(intervaloEntreEtapasMs));
	}

	@Bean
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			NotificacaoClientGateway notificacaoClientGateway) {
		return new PublicarEventosPendentesUseCase(
				alarmeRepositoryGateway, outboxEventRepositoryGateway, notificacaoClientGateway);
	}

	@Bean
	public PublicarEventosInteracaoPendentesUseCase publicarEventosInteracaoPendentesUseCase(
			EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway,
			RelatorioAdesaoClientGateway relatorioAdesaoClientGateway) {
		return new PublicarEventosInteracaoPendentesUseCase(
				eventoInteracaoRepositoryGateway, relatorioAdesaoClientGateway);
	}
}
