package com.dosealerta.relatorioadesao.core.usecase;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.core.rules.RegraCalculoTaxaAdesao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Endpoint de consulta para o profissional de saúde (Etapa 8.3): taxa de adesão do paciente
 * no período, quebrada por medicamento (Etapa 8.2).
 */
public class ConsultarTaxaAdesaoUseCase {

	private final InteracaoRepositoryGateway interacaoRepositoryGateway;

	public ConsultarTaxaAdesaoUseCase(InteracaoRepositoryGateway interacaoRepositoryGateway) {
		this.interacaoRepositoryGateway = interacaoRepositoryGateway;
	}

	public List<TaxaAdesao> executar(UUID pacienteId, Instant inicio, Instant fim) {
		List<Interacao> interacoes = interacaoRepositoryGateway.buscarPorPacienteEPeriodo(pacienteId, inicio, fim);
		return RegraCalculoTaxaAdesao.calcularPorMedicamento(interacoes, inicio, fim);
	}
}
