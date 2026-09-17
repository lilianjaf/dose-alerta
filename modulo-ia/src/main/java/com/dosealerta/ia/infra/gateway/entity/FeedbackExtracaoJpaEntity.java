package com.dosealerta.ia.infra.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback_extracao")
public class FeedbackExtracaoJpaEntity {

	@Id
	private UUID id;

	@Column(name = "receita_id", nullable = false)
	private UUID receitaId;

	@Column(name = "medicamento_extraido", nullable = false)
	private String medicamentoExtraido;

	@Column(name = "dose_extraida", nullable = false)
	private String doseExtraida;

	@Column(name = "frequencia_extraida_horas", nullable = false)
	private int frequenciaExtraidaHoras;

	@Column(name = "duracao_extraida_dias", nullable = false)
	private int duracaoExtraidaDias;

	@Column(name = "medicamento_confirmado", nullable = false)
	private String medicamentoConfirmado;

	@Column(name = "dose_confirmada", nullable = false)
	private String doseConfirmada;

	@Column(name = "frequencia_confirmada_horas", nullable = false)
	private int frequenciaConfirmadaHoras;

	@Column(name = "duracao_confirmada_dias", nullable = false)
	private int duracaoConfirmadaDias;

	@Column(nullable = false)
	private boolean corrigido;

	@Column(name = "registrado_em", nullable = false)
	private Instant registradoEm;

	protected FeedbackExtracaoJpaEntity() {
	}

	public FeedbackExtracaoJpaEntity(
			UUID id,
			UUID receitaId,
			String medicamentoExtraido,
			String doseExtraida,
			int frequenciaExtraidaHoras,
			int duracaoExtraidaDias,
			String medicamentoConfirmado,
			String doseConfirmada,
			int frequenciaConfirmadaHoras,
			int duracaoConfirmadaDias,
			boolean corrigido,
			Instant registradoEm) {
		this.id = id;
		this.receitaId = receitaId;
		this.medicamentoExtraido = medicamentoExtraido;
		this.doseExtraida = doseExtraida;
		this.frequenciaExtraidaHoras = frequenciaExtraidaHoras;
		this.duracaoExtraidaDias = duracaoExtraidaDias;
		this.medicamentoConfirmado = medicamentoConfirmado;
		this.doseConfirmada = doseConfirmada;
		this.frequenciaConfirmadaHoras = frequenciaConfirmadaHoras;
		this.duracaoConfirmadaDias = duracaoConfirmadaDias;
		this.corrigido = corrigido;
		this.registradoEm = registradoEm;
	}

	public UUID getId() {
		return id;
	}

	public UUID getReceitaId() {
		return receitaId;
	}

	public String getMedicamentoExtraido() {
		return medicamentoExtraido;
	}

	public String getDoseExtraida() {
		return doseExtraida;
	}

	public int getFrequenciaExtraidaHoras() {
		return frequenciaExtraidaHoras;
	}

	public int getDuracaoExtraidaDias() {
		return duracaoExtraidaDias;
	}

	public String getMedicamentoConfirmado() {
		return medicamentoConfirmado;
	}

	public String getDoseConfirmada() {
		return doseConfirmada;
	}

	public int getFrequenciaConfirmadaHoras() {
		return frequenciaConfirmadaHoras;
	}

	public int getDuracaoConfirmadaDias() {
		return duracaoConfirmadaDias;
	}

	public boolean isCorrigido() {
		return corrigido;
	}

	public Instant getRegistradoEm() {
		return registradoEm;
	}
}
