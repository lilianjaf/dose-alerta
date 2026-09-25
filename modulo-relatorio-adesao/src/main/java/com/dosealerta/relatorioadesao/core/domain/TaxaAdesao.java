package com.dosealerta.relatorioadesao.core.domain;

import java.time.Instant;

public record TaxaAdesao(
		String medicamento,
		Instant periodoInicio,
		Instant periodoFim,
		int totalConfirmados,
		int totalNaoConfirmados,
		int totalLigacoesAtendidas,
		Double taxaConfirmacao) {
}
