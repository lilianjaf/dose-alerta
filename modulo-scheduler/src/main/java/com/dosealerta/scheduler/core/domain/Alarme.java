package com.dosealerta.scheduler.core.domain;

import com.dosealerta.scheduler.core.exception.AlarmeJaConfirmadoException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Alarme {

	private final UUID id;
	private final UUID pacienteId;
	private final String medicamento;
	private final String dose;
	private final Instant horarioAlvo;
	private final Instant criadoEm;
	private final List<Interacao> interacoes;
	private final List<OutboxEvent> eventosOutbox;
	private StatusAlarme status;
	private EtapaEscalonamento etapaAtual;
	private Instant ultimoEnvioEm;

	private Alarme(
			UUID id,
			UUID pacienteId,
			String medicamento,
			String dose,
			Instant horarioAlvo,
			Instant criadoEm,
			StatusAlarme status,
			EtapaEscalonamento etapaAtual,
			Instant ultimoEnvioEm,
			List<Interacao> interacoes,
			List<OutboxEvent> eventosOutbox) {
		this.id = id;
		this.pacienteId = pacienteId;
		this.medicamento = medicamento;
		this.dose = dose;
		this.horarioAlvo = horarioAlvo;
		this.criadoEm = criadoEm;
		this.status = status;
		this.etapaAtual = etapaAtual;
		this.ultimoEnvioEm = ultimoEnvioEm;
		this.interacoes = new ArrayList<>(interacoes);
		this.eventosOutbox = new ArrayList<>(eventosOutbox);
	}

	public static Alarme criar(UUID pacienteId, String medicamento, String dose, Instant horarioAlvo) {
		return new Alarme(
				UUID.randomUUID(),
				pacienteId,
				medicamento,
				dose,
				horarioAlvo,
				Instant.now(),
				StatusAlarme.PENDENTE,
				null,
				null,
				List.of(),
				List.of());
	}

	public static Alarme existente(
			UUID id,
			UUID pacienteId,
			String medicamento,
			String dose,
			Instant horarioAlvo,
			Instant criadoEm,
			StatusAlarme status,
			EtapaEscalonamento etapaAtual,
			Instant ultimoEnvioEm,
			List<Interacao> interacoes,
			List<OutboxEvent> eventosOutbox) {
		return new Alarme(
				id,
				pacienteId,
				medicamento,
				dose,
				horarioAlvo,
				criadoEm,
				status,
				etapaAtual,
				ultimoEnvioEm,
				interacoes,
				eventosOutbox);
	}

	public void registrarEnvio(EtapaEscalonamento etapa, Instant quando) {
		this.etapaAtual = etapa;
		this.ultimoEnvioEm = quando;
		this.eventosOutbox.add(OutboxEvent.novo(this.id, etapa, quando));
	}

	public void confirmar(Instant quando) {
		if (status == StatusAlarme.CONFIRMADO) {
			throw new AlarmeJaConfirmadoException(id);
		}
		this.status = StatusAlarme.CONFIRMADO;
		this.interacoes.add(Interacao.nova(TipoInteracao.CONFIRMACAO, quando));
	}

	public void marcarNaoConfirmado(Instant quando) {
		this.status = StatusAlarme.NAO_CONFIRMADO;
		this.interacoes.add(Interacao.nova(TipoInteracao.NAO_CONFIRMACAO, quando));
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

	public Instant getUltimoEnvioEm() {
		return ultimoEnvioEm;
	}

	public List<Interacao> getInteracoes() {
		return List.copyOf(interacoes);
	}

	public List<OutboxEvent> getEventosOutbox() {
		return List.copyOf(eventosOutbox);
	}
}
