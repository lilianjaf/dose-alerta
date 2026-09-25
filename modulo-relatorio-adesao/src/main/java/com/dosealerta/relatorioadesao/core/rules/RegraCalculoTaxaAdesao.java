package com.dosealerta.relatorioadesao.core.rules;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class RegraCalculoTaxaAdesao {

	private RegraCalculoTaxaAdesao() {
	}

	public static List<TaxaAdesao> calcularPorMedicamento(List<Interacao> interacoes, Instant inicio, Instant fim) {
		Map<String, List<Interacao>> porMedicamento = interacoes.stream()
				.collect(Collectors.groupingBy(
						RegraCalculoTaxaAdesao::chaveDeAgrupamento, LinkedHashMap::new, Collectors.toList()));

		return porMedicamento.values().stream()
				.map(grupo -> calcular(grupo.get(0).medicamento().trim(), grupo, inicio, fim))
				.toList();
	}

	private static String chaveDeAgrupamento(Interacao interacao) {
		return interacao.medicamento().trim().toLowerCase(Locale.ROOT);
	}

	public static TaxaAdesao calcular(String medicamento, List<Interacao> interacoesDoMedicamento, Instant inicio, Instant fim) {
		int confirmados = contar(interacoesDoMedicamento, TipoInteracao.CONFIRMACAO);
		int naoConfirmados = contar(interacoesDoMedicamento, TipoInteracao.NAO_CONFIRMACAO);
		int ligacoesAtendidas = contar(interacoesDoMedicamento, TipoInteracao.LIGACAO_ATENDIDA);

		int totalComDesfecho = confirmados + naoConfirmados;
		Double taxa = totalComDesfecho == 0 ? null : confirmados / (double) totalComDesfecho;

		return new TaxaAdesao(medicamento, inicio, fim, confirmados, naoConfirmados, ligacoesAtendidas, taxa);
	}

	private static int contar(List<Interacao> interacoes, TipoInteracao tipo) {
		return (int) interacoes.stream().filter(i -> i.tipo() == tipo).count();
	}
}
