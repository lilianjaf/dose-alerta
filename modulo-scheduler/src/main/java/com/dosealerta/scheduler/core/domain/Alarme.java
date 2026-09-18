package com.dosealerta.scheduler.core.domain;

import com.dosealerta.scheduler.core.exception.AlarmeJaConfirmadoException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Alarme {

	private final UUID id;
	private final UUID pacienteId;
	private final String telefone;
	private final String medicamento;
	private final String dose;
	private final Instant horarioAlvo;
	private final Instant criadoEm;
	private final List<Interacao> interacoes;
	private final List<OutboxEvent> eventosOutbox;
	private final List<EventoInteracao> eventosInteracaoOutbox;
	private StatusAlarme status;
	private EtapaEscalonamento etapaAtual;
	private Instant ultimoEnvioEm;
	private final Long version;

	private Alarme(
			UUID id,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			Instant horarioAlvo,
			Instant criadoEm,
			StatusAlarme status,
			EtapaEscalonamento etapaAtual,
			Instant ultimoEnvioEm,
			List<Interacao> interacoes,
			List<OutboxEvent> eventosOutbox,
			List<EventoInteracao> eventosInteracaoOutbox,
			Long version) {
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
		this.interacoes = new ArrayList<>(interacoes);
		this.eventosOutbox = new ArrayList<>(eventosOutbox);
		this.eventosInteracaoOutbox = new ArrayList<>(eventosInteracaoOutbox);
		this.version = version;
	}

	public static Alarme criar(
			UUID pacienteId, String telefone, String medicamento, String dose, Instant horarioAlvo) {
		return new Alarme(
				UUID.randomUUID(),
				pacienteId,
				telefone,
				medicamento,
				dose,
				horarioAlvo,
				Instant.now(),
				StatusAlarme.PENDENTE,
				null,
				null,
				List.of(),
				List.of(),
				List.of(),
				null);
	}

	public static Alarme existente(
			UUID id,
			UUID pacienteId,
			String telefone,
			String medicamento,
			String dose,
			Instant horarioAlvo,
			Instant criadoEm,
			StatusAlarme status,
			EtapaEscalonamento etapaAtual,
			Instant ultimoEnvioEm,
			List<Interacao> interacoes,
			List<OutboxEvent> eventosOutbox,
			List<EventoInteracao> eventosInteracaoOutbox,
			Long version) {
		return new Alarme(
				id,
				pacienteId,
				telefone,
				medicamento,
				dose,
				horarioAlvo,
				criadoEm,
				status,
				etapaAtual,
				ultimoEnvioEm,
				interacoes,
				eventosOutbox,
				eventosInteracaoOutbox,
				version);
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
		registrarEventoInteracao(TipoInteracao.CONFIRMACAO, quando);
	}

	public void marcarNaoConfirmado(Instant quando) {
		this.status = StatusAlarme.NAO_CONFIRMADO;
		this.interacoes.add(Interacao.nova(TipoInteracao.NAO_CONFIRMACAO, quando));
		registrarEventoInteracao(TipoInteracao.NAO_CONFIRMACAO, quando);
	}

	/**
	 * Registra que o paciente atendeu a ligação de confirmação, sem alterar o status do
	 * alarme — atender a ligação não implica ter apertado o dígito de confirmação (ver
	 * {@link #confirmar(Instant)}), mas já é um sinal de engajamento relevante para o
	 * relatório de adesão.
	 */
	public void registrarLigacaoAtendida(Instant quando) {
		this.interacoes.add(Interacao.nova(TipoInteracao.LIGACAO_ATENDIDA, quando));
		registrarEventoInteracao(TipoInteracao.LIGACAO_ATENDIDA, quando);
	}

	private void registrarEventoInteracao(TipoInteracao tipo, Instant quando) {
		this.eventosInteracaoOutbox.add(EventoInteracao.novo(this.id, this.pacienteId, this.medicamento, tipo, quando));
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

	public List<Interacao> getInteracoes() {
		return List.copyOf(interacoes);
	}

	public List<OutboxEvent> getEventosOutbox() {
		return List.copyOf(eventosOutbox);
	}

	public List<EventoInteracao> getEventosInteracaoOutbox() {
		return List.copyOf(eventosInteracaoOutbox);
	}

	public Long getVersion() {
		return version;
	}
}
