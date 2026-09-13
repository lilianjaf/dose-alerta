package com.dosealerta.scheduler.infra.gateway.entity;

import com.dosealerta.scheduler.core.domain.TipoInteracao;
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
@Table(name = "interacao")
public class InteracaoJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alarme_id", nullable = false)
	private AlarmeJpaEntity alarme;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoInteracao tipo;

	@Column(name = "registrada_em", nullable = false)
	private Instant registradaEm;

	protected InteracaoJpaEntity() {
	}

	public InteracaoJpaEntity(UUID id, AlarmeJpaEntity alarme, TipoInteracao tipo, Instant registradaEm) {
		this.id = id;
		this.alarme = alarme;
		this.tipo = tipo;
		this.registradaEm = registradaEm;
	}

	public UUID getId() {
		return id;
	}

	public TipoInteracao getTipo() {
		return tipo;
	}

	public Instant getRegistradaEm() {
		return registradaEm;
	}
}
