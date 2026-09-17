package com.dosealerta.ia.infra.gateway.entity;

import com.dosealerta.ia.core.domain.StatusOutboxEvent;
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
	@JoinColumn(name = "receita_id", nullable = false)
	private ReceitaJpaEntity receita;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusOutboxEvent status;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Column(name = "publicado_em")
	private Instant publicadoEm;

	protected OutboxEventJpaEntity() {
	}

	public OutboxEventJpaEntity(
			UUID id, ReceitaJpaEntity receita, StatusOutboxEvent status, Instant criadoEm, Instant publicadoEm) {
		this.id = id;
		this.receita = receita;
		this.status = status;
		this.criadoEm = criadoEm;
		this.publicadoEm = publicadoEm;
	}

	public UUID getId() {
		return id;
	}

	public UUID getReceitaId() {
		return receita.getId();
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
}
