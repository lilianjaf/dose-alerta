package com.dosealerta.relatorioadesao.core.usecase;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.rules.RegraCalculoTaxaAdesao;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ConsultaTaxaAdesaoContext;
import com.dosealerta.relatorioadesao.core.rules.consultartaxa.ValidadorConsultaTaxaAdesaoRule;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ConsultarTaxaAdesaoUseCaseImpl implements ConsultarTaxaAdesaoUseCase {

	private final InteracaoRepositoryGateway interacaoRepositoryGateway;
	private final Clock clock;
	private final List<ValidadorConsultaTaxaAdesaoRule> rules;

	public ConsultarTaxaAdesaoUseCaseImpl(InteracaoRepositoryGateway interacaoRepositoryGateway, Clock clock, List<ValidadorConsultaTaxaAdesaoRule> rules) {
		this.interacaoRepositoryGateway = interacaoRepositoryGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public List<TaxaAdesao> executar(UUID pacienteId, Instant inicio, Instant fim) {
		Instant inicioResolvido = inicio != null ? inicio : Instant.EPOCH;
		Instant fimResolvido = fim != null ? fim : clock.instant();
		ConsultaTaxaAdesaoContext context = new ConsultaTaxaAdesaoContext(pacienteId, inicioResolvido, fimResolvido);
		rules.forEach(rule -> rule.validar(context));

		List<Interacao> interacoes =
				interacaoRepositoryGateway.buscarPorPacienteEPeriodo(pacienteId, inicioResolvido, fimResolvido);
		return RegraCalculoTaxaAdesao.calcularPorMedicamento(interacoes, inicioResolvido, fimResolvido);
	}
}
