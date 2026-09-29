package com.dosealerta.relatorioadesao.infra.config;

import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ConsultarTaxaPacienteIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ConsultarTaxaPeriodoDeveSerValidoRule;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ValidadorConsultaTaxaAdesaoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoAlarmeIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoMedicamentoDevePreenchidoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoPacienteIdDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoRegistradaEmDeveSerInformadaRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.RegistrarInteracaoTipoDeveSerInformadoRule;
import com.dosealerta.relatorioadesao.core.rules.registrarinteracao.ValidadorRegistroInteracaoRule;
import com.dosealerta.relatorioadesao.core.usecase.ConsultarTaxaAdesaoUseCase;
import com.dosealerta.relatorioadesao.core.usecase.ConsultarTaxaAdesaoUseCaseImpl;
import com.dosealerta.relatorioadesao.core.usecase.RegistrarInteracaoUseCase;
import com.dosealerta.relatorioadesao.core.usecase.RegistrarInteracaoUseCaseImpl;
import com.dosealerta.relatorioadesao.infra.decorator.LoggingConsultarTaxaAdesaoUseCase;
import com.dosealerta.relatorioadesao.infra.decorator.LoggingRegistrarInteracaoUseCase;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RelatorioAdesaoConfig {

	@Bean
	public RegistrarInteracaoUseCase registrarInteracaoUseCase(InteracaoRepositoryGateway interacaoRepositoryGateway) {
		List<ValidadorRegistroInteracaoRule> rules = List.of(
				new RegistrarInteracaoIdDeveSerInformadoRule(),
				new RegistrarInteracaoAlarmeIdDeveSerInformadoRule(),
				new RegistrarInteracaoPacienteIdDeveSerInformadoRule(),
				new RegistrarInteracaoMedicamentoDevePreenchidoRule(),
				new RegistrarInteracaoTipoDeveSerInformadoRule(),
				new RegistrarInteracaoRegistradaEmDeveSerInformadaRule());
		return new LoggingRegistrarInteracaoUseCase(
				new RegistrarInteracaoUseCaseImpl(interacaoRepositoryGateway, rules));
	}

	@Bean
	public ConsultarTaxaAdesaoUseCase consultarTaxaAdesaoUseCase(
			InteracaoRepositoryGateway interacaoRepositoryGateway, Clock clock) {
		List<ValidadorConsultaTaxaAdesaoRule> rules = List.of(
				new ConsultarTaxaPacienteIdDeveSerInformadoRule(), new ConsultarTaxaPeriodoDeveSerValidoRule());
		return new LoggingConsultarTaxaAdesaoUseCase(
				new ConsultarTaxaAdesaoUseCaseImpl(interacaoRepositoryGateway, clock, rules));
	}
}
