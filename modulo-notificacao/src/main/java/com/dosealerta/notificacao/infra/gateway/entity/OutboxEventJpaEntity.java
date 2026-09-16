package com.dosealerta.notificacao.infra.gateway.entity;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEventJpaEntity {

	@Id
	private UUID id;

	@Column(name = "alarme_id", nullable = false)
	private UUID alarmeId;

	@Column(name = "paciente_id", nullable = false)
	private UUID pacienteId;

	@Column(nullable = false)
	private String telefone;

	@Column(nullable = false)
	private String medicamento;

	@Column(nullable = false)
	private String dose;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EtapaEscalonamento etapa;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Canal canal;

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
			UUID id,
			UUID alarmeId,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			EtapaEscalonamento etapa,
			Canal canal,
			StatusOutboxEvent status,
			Instant criadoEm,
			Instant publicadoEm) {
		this.id = id;
		this.alarmeId = alarmeId;
		this.pacienteId = pacienteId;
		this.telefone = telefone;
		this.medicamento = medicamento;
		this.dose = dose;
		this.etapa = etapa;
		this.canal = canal;
		this.status = status;
		this.criadoEm = criadoEm;
		this.publicadoEm = publicadoEm;
	}

	public UUID getId() {
		return id;
	}

	public UUID getAlarmeId() {
		return alarmeId;
	}

	public UUID getPacienteId() {
		return pacienteId;
	}

	public String getTelefone() {
		return telefone;
	}

	public String getMedicamento() {
		return medicamento;
	}

	public String getDose() {
		return dose;
	}

	public EtapaEscalonamento getEtapa() {
		return etapa;
	}

	public Canal getCanal() {
		return canal;
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
