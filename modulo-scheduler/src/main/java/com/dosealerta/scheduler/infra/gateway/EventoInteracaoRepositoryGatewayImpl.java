package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.infra.gateway.entity.EventoInteracaoJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class EventoInteracaoRepositoryGatewayImpl implements EventoInteracaoRepositoryGateway {

	private final EventoInteracaoJpaRepository eventoInteracaoJpaRepository;

	EventoInteracaoRepositoryGatewayImpl(EventoInteracaoJpaRepository eventoInteracaoJpaRepository) {
		this.eventoInteracaoJpaRepository = eventoInteracaoJpaRepository;
	}

	@Override
	public List<EventoInteracao> buscarPendentes(int limite) {
		return eventoInteracaoJpaRepository
				.findByStatusOrderByRegistradaEmAsc(StatusOutboxEvent.PENDENTE, PageRequest.of(0, limite))
				.stream()
				.map(this::paraDominio)
				.toList();
	}

	@Override
	public void marcarComoPublicado(UUID id, Instant quando) {
		eventoInteracaoJpaRepository.findById(id).ifPresent(entidade -> {
			EventoInteracao publicado = paraDominio(entidade).publicado(quando);
			entidade.setStatus(publicado.status());
			entidade.setPublicadoEm(publicado.publicadoEm());
			eventoInteracaoJpaRepository.save(entidade);
		});
	}

	private EventoInteracao paraDominio(EventoInteracaoJpaEntity entidade) {
		return new EventoInteracao(
				entidade.getId(),
				entidade.getAlarmeId(),
				entidade.getPacienteId(),
				entidade.getMedicamento(),
				entidade.getTipo(),
				entidade.getRegistradaEm(),
				entidade.getStatus(),
				entidade.getPublicadoEm(),
				entidade.getCorrelationId());
	}
}
