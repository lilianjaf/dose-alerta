package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import com.dosealerta.scheduler.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.scheduler.infra.gateway.entity.OutboxEventJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class OutboxEventRepositoryGatewayImpl implements OutboxEventRepositoryGateway {

	private final OutboxEventJpaRepository outboxEventJpaRepository;

	OutboxEventRepositoryGatewayImpl(OutboxEventJpaRepository outboxEventJpaRepository) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
	}

	@Override
	public List<OutboxEvent> buscarPendentes(int limite) {
		return outboxEventJpaRepository
				.findByStatusOrderByCriadoEmAsc(StatusOutboxEvent.PENDENTE, PageRequest.of(0, limite))
				.stream()
				.map(this::paraDominio)
				.toList();
	}

	@Override
	public void marcarComoPublicado(UUID id, Instant quando) {
		outboxEventJpaRepository.findById(id).ifPresent(entidade -> {
			OutboxEvent publicado = paraDominio(entidade).publicado(quando);
			entidade.setStatus(publicado.status());
			entidade.setPublicadoEm(publicado.publicadoEm());
			outboxEventJpaRepository.save(entidade);
		});
	}

	private OutboxEvent paraDominio(OutboxEventJpaEntity entidade) {
		return new OutboxEvent(
				entidade.getId(),
				entidade.getAlarmeId(),
				entidade.getEtapa(),
				entidade.getStatus(),
				entidade.getCriadoEm(),
				entidade.getPublicadoEm());
	}
}
