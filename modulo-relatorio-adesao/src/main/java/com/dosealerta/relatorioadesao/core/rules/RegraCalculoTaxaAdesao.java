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

/**
 * Calcula a taxa de adesão a partir das interações de um paciente num período — agrupando
 * por medicamento (Etapa 8.2: "taxa de adesão por paciente/medicamento/período").
 *
 * <p>Só {@code CONFIRMACAO} e {@code NAO_CONFIRMACAO} entram no denominador: são os únicos
 * desfechos finais de um alarme (mutuamente exclusivos — o modulo-scheduler nunca escalona
 * um alarme já confirmado, então um mesmo alarme não pode gerar as duas). {@code
 * LIGACAO_ATENDIDA} é um sinal de engajamento que pode coexistir com qualquer desfecho (o
 * paciente pode atender a ligação e mesmo assim não confirmar) — contá-la no denominador
 * infla a base sem representar um alarme adicional, então fica só como contagem informativa.
 */
public final class RegraCalculoTaxaAdesao {

	private RegraCalculoTaxaAdesao() {
	}

	/**
	 * Agrupa por uma chave normalizada (sem espaços nas pontas, sem diferenciar maiúsculas de
	 * minúsculas) — sem isso, "Losartana" extraída pela IA numa receita e "losartana" ou
	 * "Losartana " (espaço a mais) noutra fragmentam a taxa de um único medicamento em dois
	 * grupos. O nome exibido é o da primeira ocorrência de cada grupo, só sem espaços nas
	 * pontas — normaliza para agrupar, não para exibir.
	 */
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
