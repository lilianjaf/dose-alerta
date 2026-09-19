package com.dosealerta.scheduler.infra.gateway.entity;

import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEventJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alarme_id", nullable = false)
	private AlarmeJpaEntity alarme;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EtapaEscalonamento etapa;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusOutboxEvent status;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Column(name = "publicado_em")
	private Instant publicadoEm;

	@Column(name = "correlation_id")
	private String correlationId;

	protected OutboxEventJpaEntity() {
	}

	public OutboxEventJpaEntity(
			UUID id,
			AlarmeJpaEntity alarme,
			EtapaEscalonamento etapa,
			StatusOutboxEvent status,
			Instant criadoEm,
			Instant publicadoEm,
			String correlationId) {
		this.id = id;
		this.alarme = alarme;
		this.etapa = etapa;
		this.status = status;
		this.criadoEm = criadoEm;
		this.publicadoEm = publicadoEm;
		this.correlationId = correlationId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAlarmeId() {
		return alarme.getId();
	}

	public EtapaEscalonamento getEtapa() {
		return etapa;
	}

	public StatusOutboxEvent getStatus() {
		return status;
	}

	public void setStatus(StatusOutboxEvent status) {
		this.status = status;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public Instant getPublicadoEm() {
		return publicadoEm;
	}

	public void setPublicadoEm(Instant publicadoEm) {
		this.publicadoEm = publicadoEm;
	}

	public String getCorrelationId() {
		return correlationId;
	}
}
