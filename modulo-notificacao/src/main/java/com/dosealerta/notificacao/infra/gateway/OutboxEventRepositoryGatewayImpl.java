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
	public List<OutboxEvent> buscarPendentes(int limite, Instant agora) {
		return outboxEventJpaRepository
				.buscarVencidos(StatusOutboxEvent.PENDENTE, agora, PageRequest.of(0, limite))
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

	@Override
	public void registrarFalha(UUID id, int tentativas, Instant proximaTentativaEm) {
		outboxEventJpaRepository.findById(id).ifPresent(entidade -> {
			entidade.setTentativas(tentativas);
			entidade.setProximaTentativaEm(proximaTentativaEm);
			outboxEventJpaRepository.save(entidade);
		});
	}

	@Override
	public void marcarComoFalhou(UUID id, int tentativas) {
		outboxEventJpaRepository.findById(id).ifPresent(entidade -> {
			entidade.setTentativas(tentativas);
			entidade.setProximaTentativaEm(null);
			entidade.setStatus(StatusOutboxEvent.FALHOU);
			outboxEventJpaRepository.save(entidade);
		});
	}

	@Override
	public void marcarComoExpirado(UUID id) {
		outboxEventJpaRepository.findById(id).ifPresent(entidade -> {
			entidade.setProximaTentativaEm(null);
			entidade.setStatus(StatusOutboxEvent.EXPIRADO);
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
				evento.correlationId(),
				evento.tentativas(),
				evento.proximaTentativaEm());
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
				entidade.getCorrelationId(),
				entidade.getTentativas(),
				entidade.getProximaTentativaEm());
	}
}
