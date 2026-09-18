package com.dosealerta.relatorioadesao.core.domain;

import java.time.Instant;

/**
 * Read model de adesão por paciente/medicamento/período (seção 4 do resumo técnico: não é
 * um agregado com identidade própria, é uma projeção calculada em cima das interações).
 *
 * <p>{@code taxaConfirmacao} é {@code null} quando não há nenhum alarme com desfecho no
 * período (nem confirmado, nem não confirmado) — distingue "sem dados" de "0% de adesão".
 * {@code totalLigacoesAtendidas} é informativo (engajamento), não entra no cálculo da taxa —
 * ver {@code RegraCalculoTaxaAdesao}.
 */
public record TaxaAdesao(
		String medicamento,
		Instant periodoInicio,
		Instant periodoFim,
		int totalConfirmados,
		int totalNaoConfirmados,
		int totalLigacoesAtendidas,
		Double taxaConfirmacao) {
}
