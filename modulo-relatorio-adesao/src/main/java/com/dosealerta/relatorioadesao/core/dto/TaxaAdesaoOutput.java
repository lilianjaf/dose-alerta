package com.dosealerta.relatorioadesao.core.dto;

import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import java.time.Instant;

public record TaxaAdesaoOutput(
		String medicamento,
		Instant periodoInicio,
		Instant periodoFim,
		int totalConfirmados,
		int totalNaoConfirmados,
		int totalLigacoesAtendidas,
		Double taxaConfirmacao) {

	public static TaxaAdesaoOutput de(TaxaAdesao taxaAdesao) {
		return new TaxaAdesaoOutput(
				taxaAdesao.medicamento(),
				taxaAdesao.periodoInicio(),
				taxaAdesao.periodoFim(),
				taxaAdesao.totalConfirmados(),
				taxaAdesao.totalNaoConfirmados(),
				taxaAdesao.totalLigacoesAtendidas(),
				taxaAdesao.taxaConfirmacao());
	}
}
