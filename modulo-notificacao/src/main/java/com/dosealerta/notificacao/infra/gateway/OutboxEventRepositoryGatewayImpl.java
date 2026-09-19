package com.dosealerta.notificacao.infra.gateway;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.notificacao.infra.gateway.entity.OutboxEventJpaEntity;
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
	public OutboxEvent salvar(OutboxEvent evento) {
		return paraDominio(outboxEventJpaRepository.save(paraEntidade(evento)));
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

	private OutboxEventJpaEntity paraEntidade(OutboxEvent evento) {
		return new OutboxEventJpaEntity(
				evento.id(),
				evento.alarmeId(),
				evento.pacienteId(),
				evento.telefone(),
				evento.medicamento(),
				evento.dose(),
				evento.etapa(),
				evento.canal(),
				evento.status(),
				evento.criadoEm(),
				evento.publicadoEm(),
				evento.correlationId());
	}

	private OutboxEvent paraDominio(OutboxEventJpaEntity entidade) {
		return new OutboxEvent(
				entidade.getId(),
				entidade.getAlarmeId(),
				entidade.getPacienteId(),
				entidade.getTelefone(),
				entidade.getMedicamento(),
				entidade.getDose(),
				entidade.getEtapa(),
				entidade.getCanal(),
				entidade.getStatus(),
				entidade.getCriadoEm(),
				entidade.getPublicadoEm(),
				entidade.getCorrelationId());
	}
}
