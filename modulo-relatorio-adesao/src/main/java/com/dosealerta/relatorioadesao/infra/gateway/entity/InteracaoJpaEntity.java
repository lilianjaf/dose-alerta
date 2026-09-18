package com.dosealerta.relatorioadesao.infra.gateway.entity;

import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interacao")
public class InteracaoJpaEntity {

	@Id
	private UUID id;

	@Column(name = "paciente_id", nullable = false)
	private UUID pacienteId;

	@Column(nullable = false)
	private String medicamento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoInteracao tipo;

	@Column(name = "registrada_em", nullable = false)
	private Instant registradaEm;

	protected InteracaoJpaEntity() {
	}

	public InteracaoJpaEntity(UUID id, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant registradaEm) {
		this.id = id;
		this.pacienteId = pacienteId;
		this.medicamento = medicamento;
		this.tipo = tipo;
		this.registradaEm = registradaEm;
	}

	public UUID getId() {
		return id;
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
}
