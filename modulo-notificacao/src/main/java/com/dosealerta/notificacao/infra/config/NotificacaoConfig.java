package com.dosealerta.notificacao.infra.config;

import com.dosealerta.notificacao.core.gateway.CorrelacaoGateway;
import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.gateway.LogGateway;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioAlarmeIdDeveSerInformadoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioDoseDevePreenchidaRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioEtapaDeveSerInformadaRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioMedicamentoDevePreenchidoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioPacienteIdDeveSerInformadoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioTelefoneDevePreenchidoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.SolicitarEnvioTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.notificacao.core.rules.solicitarenvio.ValidadorSolicitacaoEnvioRule;
import com.dosealerta.notificacao.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.notificacao.core.usecase.PublicarEventosPendentesUseCaseImpl;
import com.dosealerta.notificacao.core.usecase.SolicitarEnvioUseCase;
import com.dosealerta.notificacao.core.usecase.SolicitarEnvioUseCaseImpl;
import com.dosealerta.notificacao.infra.decorator.LoggingPublicarEventosPendentesUseCase;
import com.dosealerta.notificacao.infra.decorator.LoggingSolicitarEnvioUseCase;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificacaoConfig {

	@Bean
	public SolicitarEnvioUseCase solicitarEnvioUseCase(
			EstrategiaCanalGateway estrategiaCanalGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		List<ValidadorSolicitacaoEnvioRule> rules = List.of(
				new SolicitarEnvioAlarmeIdDeveSerInformadoRule(),
				new SolicitarEnvioPacienteIdDeveSerInformadoRule(),
				new SolicitarEnvioTelefoneDevePreenchidoRule(),
				new SolicitarEnvioTelefoneDeveTerFormatoValidoRule(),
				new SolicitarEnvioMedicamentoDevePreenchidoRule(),
				new SolicitarEnvioDoseDevePreenchidaRule(),
				new SolicitarEnvioEtapaDeveSerInformadaRule());
		return new LoggingSolicitarEnvioUseCase(new SolicitarEnvioUseCaseImpl(
				estrategiaCanalGateway, outboxEventRepositoryGateway, correlacaoGateway, clock, rules));
	}

	@Bean
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			MensageriaClientGateway mensageriaClientGateway,
			LogGateway logGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		return new LoggingPublicarEventosPendentesUseCase(new PublicarEventosPendentesUseCaseImpl(
				outboxEventRepositoryGateway, mensageriaClientGateway, logGateway, correlacaoGateway, clock));
	}
}
