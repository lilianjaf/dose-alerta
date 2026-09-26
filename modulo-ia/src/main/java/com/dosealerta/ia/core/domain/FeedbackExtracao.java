package com.dosealerta.ia.core.domain;

import java.time.Instant;
import java.util.UUID;

public record FeedbackExtracao(
		UUID id,
		UUID receitaId,
		String medicamentoExtraido,
		String doseExtraida,
		Integer frequenciaExtraidaHoras,
		Integer duracaoExtraidaDias,
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
