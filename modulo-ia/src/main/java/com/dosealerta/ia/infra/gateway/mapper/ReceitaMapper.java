package com.dosealerta.ia.infra.gateway.mapper;

import com.dosealerta.ia.core.domain.OutboxEvent;
import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.infra.gateway.entity.OutboxEventJpaEntity;
import com.dosealerta.ia.infra.gateway.entity.ReceitaJpaEntity;
import java.util.List;

public final class ReceitaMapper {

	private ReceitaMapper() {
	}

	public static ReceitaJpaEntity paraEntidade(Receita receita) {
		ReceitaJpaEntity entidade = new ReceitaJpaEntity(
				receita.getId(),
				receita.getPacienteId(),
				receita.getTelefone(),
				receita.getMedicamento(),
				receita.getDose(),
				receita.getFrequenciaHoras(),
				receita.getDuracaoDias(),
				receita.getHorarioInicial(),
				receita.getCriadoEm(),
				receita.getStatus());

		for (OutboxEvent evento : receita.getEventosOutbox()) {
			entidade.adicionarEventoOutbox(new OutboxEventJpaEntity(
					evento.id(), entidade, evento.status(), evento.criadoEm(), evento.publicadoEm(), evento.correlationId()));
		}

		return entidade;
	}

	public static Receita paraDominio(ReceitaJpaEntity entidade) {
		List<OutboxEvent> eventosOutbox = entidade.getEventosOutbox().stream()
				.map(e -> new OutboxEvent(
						e.getId(), e.getReceitaId(), e.getStatus(), e.getCriadoEm(), e.getPublicadoEm(), e.getCorrelationId()))
				.toList();

		return Receita.existente(
				entidade.getId(),
				entidade.getPacienteId(),
				entidade.getTelefone(),
				entidade.getMedicamento(),
				entidade.getDose(),
				entidade.getFrequenciaHoras(),
				entidade.getDuracaoDias(),
				entidade.getHorarioInicial(),
				entidade.getCriadoEm(),
				entidade.getStatus(),
				eventosOutbox);
	}
}
