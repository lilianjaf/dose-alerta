package com.dosealerta.scheduler.infra.gateway.entity;

import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "alarme")
public class AlarmeJpaEntity {

	@Id
	private UUID id;

	@Column(name = "paciente_id", nullable = false)
	private UUID pacienteId;

	@Column(nullable = false)
	private String telefone;

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

	@Column(name = "ultimo_envio_em")
	private Instant ultimoEnvioEm;

	@OneToMany(mappedBy = "alarme", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("registradaEm asc")
	@BatchSize(size = 20)
	private List<InteracaoJpaEntity> interacoes = new ArrayList<>();

	@OneToMany(mappedBy = "alarme", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("criadoEm asc")
	@BatchSize(size = 20)
	private List<OutboxEventJpaEntity> eventosOutbox = new ArrayList<>();

	protected AlarmeJpaEntity() {
	}

	public AlarmeJpaEntity(
			UUID id,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			Instant horarioAlvo,
			Instant criadoEm,
			StatusAlarme status,
			EtapaEscalonamento etapaAtual,
			Instant ultimoEnvioEm) {
		this.id = id;
		this.pacienteId = pacienteId;
		this.telefone = telefone;
		this.medicamento = medicamento;
		this.dose = dose;
		this.horarioAlvo = horarioAlvo;
		this.criadoEm = criadoEm;
		this.status = status;
		this.etapaAtual = etapaAtual;
		this.ultimoEnvioEm = ultimoEnvioEm;
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

	public String getTelefone() {
		return telefone;
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

	public Instant getUltimoEnvioEm() {
		return ultimoEnvioEm;
	}

	public List<InteracaoJpaEntity> getInteracoes() {
		return interacoes;
	}

	public List<OutboxEventJpaEntity> getEventosOutbox() {
		return eventosOutbox;
	}
}
