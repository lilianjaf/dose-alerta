package com.dosealerta.ia.infra.gateway.entity;

import com.dosealerta.ia.core.domain.StatusReceita;
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
@Table(name = "receita")
public class ReceitaJpaEntity {

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

	@Column(name = "frequencia_horas", nullable = false)
	private int frequenciaHoras;

	@Column(name = "duracao_dias", nullable = false)
	private int duracaoDias;

	@Column(name = "horario_inicial", nullable = false)
	private Instant horarioInicial;

	@Column(name = "criado_em", nullable = false)
	private Instant criadoEm;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusReceita status;

	@OneToMany(mappedBy = "receita", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("criadoEm asc")
	@BatchSize(size = 20)
	private List<OutboxEventJpaEntity> eventosOutbox = new ArrayList<>();

	protected ReceitaJpaEntity() {
	}

	public ReceitaJpaEntity(
			UUID id,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			int frequenciaHoras,
			int duracaoDias,
			Instant horarioInicial,
			Instant criadoEm,
			StatusReceita status) {
		this.id = id;
		this.pacienteId = pacienteId;
		this.telefone = telefone;
		this.medicamento = medicamento;
		this.dose = dose;
		this.frequenciaHoras = frequenciaHoras;
		this.duracaoDias = duracaoDias;
		this.horarioInicial = horarioInicial;
		this.criadoEm = criadoEm;
		this.status = status;
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

	public int getFrequenciaHoras() {
		return frequenciaHoras;
	}

	public int getDuracaoDias() {
		return duracaoDias;
	}

	public Instant getHorarioInicial() {
		return horarioInicial;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public StatusReceita getStatus() {
		return status;
	}

	public List<OutboxEventJpaEntity> getEventosOutbox() {
		return eventosOutbox;
	}
}
