package com.dosealerta.scheduler.infra.gateway.mapper;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.Interacao;
import com.dosealerta.scheduler.core.domain.OutboxEvent;
import com.dosealerta.scheduler.infra.gateway.entity.AlarmeJpaEntity;
import com.dosealerta.scheduler.infra.gateway.entity.InteracaoJpaEntity;
import com.dosealerta.scheduler.infra.gateway.entity.OutboxEventJpaEntity;
import java.util.List;

public final class AlarmeMapper {

	private AlarmeMapper() {
	}

	public static AlarmeJpaEntity paraEntidade(Alarme alarme) {
		AlarmeJpaEntity entidade = new AlarmeJpaEntity(
				alarme.getId(),
				alarme.getPacienteId(),
				alarme.getMedicamento(),
				alarme.getDose(),
				alarme.getHorarioAlvo(),
				alarme.getCriadoEm(),
				alarme.getStatus(),
				alarme.getEtapaAtual(),
				alarme.getUltimoEnvioEm());

		for (Interacao interacao : alarme.getInteracoes()) {
			entidade.adicionarInteracao(
					new InteracaoJpaEntity(interacao.id(), entidade, interacao.tipo(), interacao.registradaEm()));
		}

		for (OutboxEvent evento : alarme.getEventosOutbox()) {
			entidade.adicionarEventoOutbox(new OutboxEventJpaEntity(
					evento.id(), entidade, evento.etapa(), evento.status(), evento.criadoEm(), evento.publicadoEm()));
		}

		return entidade;
	}

	public static Alarme paraDominio(AlarmeJpaEntity entidade) {
		List<Interacao> interacoes = entidade.getInteracoes().stream()
				.map(i -> new Interacao(i.getId(), i.getTipo(), i.getRegistradaEm()))
				.toList();

		List<OutboxEvent> eventosOutbox = entidade.getEventosOutbox().stream()
				.map(e -> new OutboxEvent(e.getId(), e.getAlarmeId(), e.getEtapa(), e.getStatus(), e.getCriadoEm(), e.getPublicadoEm()))
				.toList();

		return Alarme.existente(
				entidade.getId(),
				entidade.getPacienteId(),
				entidade.getMedicamento(),
				entidade.getDose(),
				entidade.getHorarioAlvo(),
				entidade.getCriadoEm(),
				entidade.getStatus(),
				entidade.getEtapaAtual(),
				entidade.getUltimoEnvioEm(),
				interacoes,
				eventosOutbox);
	}
}
