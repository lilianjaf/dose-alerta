package com.dosealerta.ia.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra, para cada receita processada, o que a IA extraiu e o que o paciente efetivamente
 * confirmou — a base rotulada mencionada na seção 6.3 do resumo técnico, usada tanto para
 * medir a qualidade do pipeline quanto como candidata a dataset de ajuste fino futuro.
 */
public record FeedbackExtracao(
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

	public static FeedbackExtracao registrar(
			Receita receitaAntesDaConfirmacao,
			String medicamentoConfirmado,
			String doseConfirmada,
			int frequenciaConfirmadaHoras,
			int duracaoConfirmadaDias,
			boolean corrigido,
			Instant quando) {
		return new FeedbackExtracao(
				UUID.randomUUID(),
				receitaAntesDaConfirmacao.getId(),
				receitaAntesDaConfirmacao.getMedicamento(),
				receitaAntesDaConfirmacao.getDose(),
				receitaAntesDaConfirmacao.getFrequenciaHoras(),
				receitaAntesDaConfirmacao.getDuracaoDias(),
				medicamentoConfirmado,
				doseConfirmada,
				frequenciaConfirmadaHoras,
				duracaoConfirmadaDias,
				corrigido,
				quando);
	}
}
