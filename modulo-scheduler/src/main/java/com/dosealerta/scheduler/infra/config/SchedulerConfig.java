package com.dosealerta.scheduler.infra.config;

import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import com.dosealerta.scheduler.core.rules.buscar.BuscarAlarmeDeveExistirRule;
import com.dosealerta.scheduler.core.rules.buscar.BuscarAlarmeIdDeveSerInformadoRule;
import com.dosealerta.scheduler.core.rules.buscar.ValidadorBuscaAlarmeRule;
import com.dosealerta.scheduler.core.rules.criar.CriarDoseDevePreenchidaRule;
import com.dosealerta.scheduler.core.rules.criar.CriarHorarioAlvoDeveSerInformadoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarMedicamentoDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarPacienteIdDeveSerInformadoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarTelefoneDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.criar.CriarTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.scheduler.core.rules.criar.ValidadorCriacaoAlarmeRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistrarConfirmacaoAlarmeNaoDeveEstarConfirmadoRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistrarConfirmacaoAlarmePendenteDeveExistirRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistrarConfirmacaoTelefoneDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.RegistrarConfirmacaoTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.ValidadorRegistroConfirmacaoRule;
import com.dosealerta.scheduler.core.rules.registrarligacao.RegistrarLigacaoAlarmePendenteDeveExistirRule;
import com.dosealerta.scheduler.core.rules.registrarligacao.RegistrarLigacaoTelefoneDevePreenchidoRule;
import com.dosealerta.scheduler.core.rules.registrarligacao.RegistrarLigacaoTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.scheduler.core.rules.registrarligacao.ValidadorRegistroLigacaoRule;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCaseImpl;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCaseImpl;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCaseImpl;
import com.dosealerta.scheduler.core.usecase.PublicarEventosInteracaoPendentesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosInteracaoPendentesUseCaseImpl;
import com.dosealerta.scheduler.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosPendentesUseCaseImpl;
import com.dosealerta.scheduler.core.usecase.RegistrarConfirmacaoUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarConfirmacaoUseCaseImpl;
import com.dosealerta.scheduler.core.usecase.RegistrarLigacaoAtendidaUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarLigacaoAtendidaUseCaseImpl;
import com.dosealerta.scheduler.infra.decorator.LoggingBuscarAlarmeUseCase;
import com.dosealerta.scheduler.infra.decorator.LoggingCriarAlarmeUseCase;
import com.dosealerta.scheduler.infra.decorator.LoggingEscalonarAlarmesUseCase;
import com.dosealerta.scheduler.infra.decorator.LoggingPublicarEventosInteracaoPendentesUseCase;
import com.dosealerta.scheduler.infra.decorator.LoggingPublicarEventosPendentesUseCase;
import com.dosealerta.scheduler.infra.decorator.LoggingRegistrarConfirmacaoUseCase;
import com.dosealerta.scheduler.infra.decorator.LoggingRegistrarLigacaoAtendidaUseCase;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

	@Bean
	public CriarAlarmeUseCase criarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway, Clock clock) {
		List<ValidadorCriacaoAlarmeRule> rules = List.of(
				new CriarPacienteIdDeveSerInformadoRule(),
				new CriarTelefoneDevePreenchidoRule(),
				new CriarMedicamentoDevePreenchidoRule(),
				new CriarDoseDevePreenchidaRule(),
				new CriarHorarioAlvoDeveSerInformadoRule(),
				new CriarTelefoneDeveTerFormatoValidoRule());
		return new LoggingCriarAlarmeUseCase(new CriarAlarmeUseCaseImpl(alarmeRepositoryGateway, clock, rules));
	}

	@Bean
	public BuscarAlarmeUseCase buscarAlarmeUseCase(AlarmeRepositoryGateway alarmeRepositoryGateway) {
		List<ValidadorBuscaAlarmeRule> rules =
				List.of(new BuscarAlarmeIdDeveSerInformadoRule(), new BuscarAlarmeDeveExistirRule());
		return new LoggingBuscarAlarmeUseCase(new BuscarAlarmeUseCaseImpl(alarmeRepositoryGateway, rules));
	}

	@Bean
	public RegistrarConfirmacaoUseCase registrarConfirmacaoUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			MetricasAlarmeGateway metricasAlarmeGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		List<ValidadorRegistroConfirmacaoRule> rules = List.of(
				new RegistrarConfirmacaoTelefoneDevePreenchidoRule(),
				new RegistrarConfirmacaoTelefoneDeveTerFormatoValidoRule(),
				new RegistrarConfirmacaoAlarmePendenteDeveExistirRule(),
				new RegistrarConfirmacaoAlarmeNaoDeveEstarConfirmadoRule());
		return new LoggingRegistrarConfirmacaoUseCase(new RegistrarConfirmacaoUseCaseImpl(
				alarmeRepositoryGateway, metricasAlarmeGateway, correlacaoGateway, clock, rules));
	}

	@Bean
	public RegistrarLigacaoAtendidaUseCase registrarLigacaoAtendidaUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway, CorrelacaoGateway correlacaoGateway, Clock clock) {
		List<ValidadorRegistroLigacaoRule> rules = List.of(
				new RegistrarLigacaoTelefoneDevePreenchidoRule(), new RegistrarLigacaoTelefoneDeveTerFormatoValidoRule(),
				new RegistrarLigacaoAlarmePendenteDeveExistirRule());
		return new LoggingRegistrarLigacaoAtendidaUseCase(
				new RegistrarLigacaoAtendidaUseCaseImpl(alarmeRepositoryGateway, correlacaoGateway, clock, rules));
	}

	@Bean
	public EscalonarAlarmesUseCase escalonarAlarmesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			MetricasAlarmeGateway metricasAlarmeGateway,
			LogGateway logGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock,
			@Value("${scheduler.escalonamento.intervalo-entre-etapas-ms:900000}") long intervaloEntreEtapasMs) {
		return new LoggingEscalonarAlarmesUseCase(new EscalonarAlarmesUseCaseImpl(
				alarmeRepositoryGateway,
				metricasAlarmeGateway,
				logGateway,
				correlacaoGateway,
				clock,
				Duration.ofMillis(intervaloEntreEtapasMs)));
	}

	@Bean
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			AlarmeRepositoryGateway alarmeRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			NotificacaoClientGateway notificacaoClientGateway,
			LogGateway logGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		return new LoggingPublicarEventosPendentesUseCase(new PublicarEventosPendentesUseCaseImpl(
				alarmeRepositoryGateway,
				outboxEventRepositoryGateway,
				notificacaoClientGateway,
				logGateway,
				correlacaoGateway,
				clock));
	}

	@Bean
	public PublicarEventosInteracaoPendentesUseCase publicarEventosInteracaoPendentesUseCase(
			EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway,
			RelatorioAdesaoClientGateway relatorioAdesaoClientGateway,
			LogGateway logGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		return new LoggingPublicarEventosInteracaoPendentesUseCase(new PublicarEventosInteracaoPendentesUseCaseImpl(
				eventoInteracaoRepositoryGateway, relatorioAdesaoClientGateway, logGateway, correlacaoGateway, clock));
	}
}
