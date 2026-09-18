package com.dosealerta.relatorioadesao.infra.gateway;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.gateway.InteracaoRepositoryGateway;
import com.dosealerta.relatorioadesao.infra.gateway.entity.InteracaoJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class InteracaoRepositoryGatewayImpl implements InteracaoRepositoryGateway {

	private final InteracaoJpaRepository interacaoJpaRepository;

	InteracaoRepositoryGatewayImpl(InteracaoJpaRepository interacaoJpaRepository) {
		this.interacaoJpaRepository = interacaoJpaRepository;
	}

	@Override
	public void salvar(Interacao interacao) {
		if (interacaoJpaRepository.existsById(interacao.id())) {
			return;
		}
		interacaoJpaRepository.save(new InteracaoJpaEntity(
				interacao.id(), interacao.pacienteId(), interacao.medicamento(), interacao.tipo(), interacao.registradaEm()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Interacao> buscarPorPacienteEPeriodo(UUID pacienteId, Instant inicio, Instant fim) {
		return interacaoJpaRepository.findByPacienteIdAndRegistradaEmBetween(pacienteId, inicio, fim).stream()
				.map(e -> new Interacao(e.getId(), e.getPacienteId(), e.getMedicamento(), e.getTipo(), e.getRegistradaEm()))
				.toList();
	}
}
