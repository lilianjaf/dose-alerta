package com.dosealerta.ia.core.domain;

import com.dosealerta.ia.core.exception.ReceitaJaConfirmadaException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Receita {

	private final UUID id;
	private final UUID pacienteId;
	private final String telefone;
	private String medicamento;
	private String dose;
	private Integer frequenciaHoras;
	private Integer duracaoDias;
	private final Instant horarioInicial;
	private final Instant criadoEm;
	private StatusReceita status;
	private final List<OutboxEvent> eventosOutbox;

	private Receita(
			UUID id,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			Integer frequenciaHoras,
			Integer duracaoDias,
			Instant horarioInicial,
			Instant criadoEm,
			StatusReceita status,
			List<OutboxEvent> eventosOutbox) {
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
		this.eventosOutbox = new ArrayList<>(eventosOutbox);
	}

	public static Receita aguardandoConfirmacao(
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			Integer frequenciaHoras,
			Integer duracaoDias,
			Instant horarioInicial) {
		return new Receita(
				UUID.randomUUID(),
				pacienteId,
				telefone,
				medicamento,
				dose,
				frequenciaHoras,
				duracaoDias,
				horarioInicial,
				Instant.now(),
				StatusReceita.AGUARDANDO_CONFIRMACAO,
				List.of());
	}

	public static Receita existente(
			UUID id,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			Integer frequenciaHoras,
			Integer duracaoDias,
			Instant horarioInicial,
			Instant criadoEm,
			StatusReceita status,
			List<OutboxEvent> eventosOutbox) {
		return new Receita(
				id,
				pacienteId,
				telefone,
				medicamento,
				dose,
				frequenciaHoras,
				duracaoDias,
				horarioInicial,
				criadoEm,
				status,
				eventosOutbox);
	}

	public boolean difereDe(String medicamento, String dose, int frequenciaHoras, int duracaoDias) {
		return !this.medicamento.equals(medicamento)
				|| !Objects.equals(this.dose, dose)
				|| !Objects.equals(this.frequenciaHoras, frequenciaHoras)
				|| !Objects.equals(this.duracaoDias, duracaoDias);
	}

	public List<String> camposPendentes() {
		List<String> pendentes = new ArrayList<>();
		if (dose == null || dose.isBlank()) {
			pendentes.add("dose");
		}
		if (frequenciaHoras == null) {
			pendentes.add("frequenciaHoras");
		}
		if (duracaoDias == null) {
			pendentes.add("duracaoDias");
		}
		return pendentes;
	}

	public void confirmar(String medicamento, String dose, int frequenciaHoras, int duracaoDias, Instant quando) {
		if (status == StatusReceita.CONFIRMADA) {
			throw new ReceitaJaConfirmadaException(id);
		}
		this.medicamento = medicamento;
		this.dose = dose;
		this.frequenciaHoras = frequenciaHoras;
		this.duracaoDias = duracaoDias;
		this.status = StatusReceita.CONFIRMADA;
		this.eventosOutbox.add(OutboxEvent.novo(this.id, quando));
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

	public Integer getFrequenciaHoras() {
		return frequenciaHoras;
	}

	public Integer getDuracaoDias() {
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

	public List<OutboxEvent> getEventosOutbox() {
		return List.copyOf(eventosOutbox);
	}
}
