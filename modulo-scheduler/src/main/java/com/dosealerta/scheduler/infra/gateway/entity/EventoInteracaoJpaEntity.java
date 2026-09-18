package com.dosealerta.scheduler.infra.gateway.entity;

import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
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
@Table(name = "evento_interacao_outbox")
public class EventoInteracaoJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "alarme_id", nullable = false)
	private AlarmeJpaEntity alarme;

	@Column(name = "paciente_id", nullable = false)
	private UUID pacienteId;

	@Column(nullable = false)
	private String medicamento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoInteracao tipo;

	@Column(name = "registrada_em", nullable = false)
	private Instant registradaEm;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusOutboxEvent status;

	@Column(name = "publicado_em")
	private Instant publicadoEm;

	protected EventoInteracaoJpaEntity() {
	}

	public EventoInteracaoJpaEntity(
			UUID id,
			AlarmeJpaEntity alarme,
			UUID pacienteId,
			String medicamento,
			TipoInteracao tipo,
			Instant registradaEm,
			StatusOutboxEvent status,
			Instant publicadoEm) {
		this.id = id;
		this.alarme = alarme;
		this.pacienteId = pacienteId;
		this.medicamento = medicamento;
		this.tipo = tipo;
		this.registradaEm = registradaEm;
		this.status = status;
		this.publicadoEm = publicadoEm;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAlarmeId() {
		return alarme.getId();
	}

	public UUID getPacienteId() {
		return pacienteId;
	}

	public String getMedicamento() {
		return medicamento;
	}

	public TipoInteracao getTipo() {
		return tipo;
	}

	public Instant getRegistradaEm() {
		return registradaEm;
	}

	public StatusOutboxEvent getStatus() {
		return status;
	}

	public void setStatus(StatusOutboxEvent status) {
		this.status = status;
	}

	public Instant getPublicadoEm() {
		return publicadoEm;
	}

	public void setPublicadoEm(Instant publicadoEm) {
		this.publicadoEm = publicadoEm;
	}
}
