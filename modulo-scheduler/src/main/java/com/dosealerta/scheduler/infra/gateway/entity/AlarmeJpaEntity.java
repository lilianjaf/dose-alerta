package com.dosealerta.scheduler.infra.gateway.entity;

import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alarme")
public class AlarmeJpaEntity {

	@Id
	private UUID id;

	@Column(name = "paciente_id", nullable = false)
	private UUID pacienteId;

	@Column(nullable = false)
	private String medicamento;

	@Column(nullable = false)
	private String dose;

	@Column(name = "horario_alvo", nullable = false)
	private Instant horarioAlvo;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusAlarme status;

	@Enumerated(EnumType.STRING)
	@Column(name = "etapa_atual")
	private EtapaEscalonamento etapaAtual;

	@OneToMany(mappedBy = "alarme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("registradaEm asc")
	private List<InteracaoJpaEntity> interacoes = new ArrayList<>();

	@OneToMany(mappedBy = "alarme", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("criadoEm asc")
	private List<OutboxEventJpaEntity> eventosOutbox = new ArrayList<>();

	protected AlarmeJpaEntity() {
	}

	public AlarmeJpaEntity(
			UUID id,
			UUID pacienteId,
			String medicamento,
			String dose,
			Instant horarioAlvo,
			Instant criadoEm,
			StatusAlarme status,
			EtapaEscalonamento etapaAtual) {
		this.id = id;
		this.pacienteId = pacienteId;
		this.medicamento = medicamento;
		this.dose = dose;
		this.horarioAlvo = horarioAlvo;
		this.criadoEm = criadoEm;
		this.status = status;
		this.etapaAtual = etapaAtual;
	}

	public void adicionarInteracao(InteracaoJpaEntity interacao) {
		this.interacoes.add(interacao);
	}

	public void adicionarEventoOutbox(OutboxEventJpaEntity evento) {
		this.eventosOutbox.add(evento);
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

	public String getDose() {
		return dose;
	}

	public Instant getHorarioAlvo() {
		return horarioAlvo;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public StatusAlarme getStatus() {
		return status;
	}

	public EtapaEscalonamento getEtapaAtual() {
		return etapaAtual;
	}

	public List<InteracaoJpaEntity> getInteracoes() {
		return interacoes;
	}

	public List<OutboxEventJpaEntity> getEventosOutbox() {
		return eventosOutbox;
	}
}
