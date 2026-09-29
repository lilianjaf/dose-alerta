package com.dosealerta.relatorioadesao.core.rules;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.idDaInteracao;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.umaInteracao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dosealerta.relatorioadesao.TesteUnitarioBase;
import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegraCalculoTaxaAdesaoTest extends TesteUnitarioBase {

	@Test
	void deveCalcularTaxaComBaseApenasEmConfirmacoesENaoConfirmacoes() {
		List<Interacao> interacoes = List.of(
				interacao(TipoInteracao.CONFIRMACAO),
				interacao(TipoInteracao.CONFIRMACAO),
				interacao(TipoInteracao.CONFIRMACAO),
				interacao(TipoInteracao.NAO_CONFIRMACAO));

		TaxaAdesao taxa = RegraCalculoTaxaAdesao.calcular("Losartana", interacoes, INSTANTE_FIXO, INSTANTE_FIXO);

		assertEquals(3, taxa.totalConfirmados());
		assertEquals(1, taxa.totalNaoConfirmados());
		assertEquals(0.75, taxa.taxaConfirmacao());
	}

	@Test
	void naoDeveContarLigacaoAtendidaNoDenominador() {
		List<Interacao> interacoes = List.of(
				interacao(TipoInteracao.CONFIRMACAO), interacao(TipoInteracao.LIGACAO_ATENDIDA),
				interacao(TipoInteracao.LIGACAO_ATENDIDA));

		TaxaAdesao taxa = RegraCalculoTaxaAdesao.calcular("Losartana", interacoes, INSTANTE_FIXO, INSTANTE_FIXO);

		assertEquals(1, taxa.totalConfirmados());
		assertEquals(0, taxa.totalNaoConfirmados());
		assertEquals(2, taxa.totalLigacoesAtendidas());
		assertEquals(1.0, taxa.taxaConfirmacao());
	}

	@Test
	void deveRetornarTaxaNulaQuandoNaoHaDesfechoNoPeriodo() {
		List<Interacao> interacoes = List.of(interacao(TipoInteracao.LIGACAO_ATENDIDA));

		TaxaAdesao taxa = RegraCalculoTaxaAdesao.calcular("Losartana", interacoes, INSTANTE_FIXO, INSTANTE_FIXO);

		assertNull(taxa.taxaConfirmacao());
	}

	@Test
	void deveAgruparPorMedicamento() {
		List<Interacao> interacoes = List.of(
				umaInteracao(idDaInteracao(1), PACIENTE_ID, "Losartana", TipoInteracao.CONFIRMACAO),
				umaInteracao(idDaInteracao(2), PACIENTE_ID, "Metformina", TipoInteracao.NAO_CONFIRMACAO));

		List<TaxaAdesao> taxas = RegraCalculoTaxaAdesao.calcularPorMedicamento(interacoes, INSTANTE_FIXO, INSTANTE_FIXO);

		assertEquals(2, taxas.size());
		assertEquals(
				1.0,
				taxas.stream().filter(t -> t.medicamento().equals("Losartana")).findFirst().orElseThrow().taxaConfirmacao());
		assertEquals(
				0.0,
				taxas.stream().filter(t -> t.medicamento().equals("Metformina")).findFirst().orElseThrow().taxaConfirmacao());
	}

	@Test
	void deveConsolidarVariacoesDeGrafiaDoMesmoMedicamentoNoAgrupamento() {
		List<Interacao> interacoes = List.of(
				umaInteracao(idDaInteracao(1), PACIENTE_ID, "Losartana", TipoInteracao.CONFIRMACAO),
				umaInteracao(idDaInteracao(3), PACIENTE_ID, "losartana", TipoInteracao.CONFIRMACAO),
				umaInteracao(idDaInteracao(4), PACIENTE_ID, " Losartana ", TipoInteracao.NAO_CONFIRMACAO));

		List<TaxaAdesao> taxas = RegraCalculoTaxaAdesao.calcularPorMedicamento(interacoes, INSTANTE_FIXO, INSTANTE_FIXO);

		assertEquals(1, taxas.size());
		TaxaAdesao taxa = taxas.get(0);
		assertEquals("Losartana", taxa.medicamento());
		assertEquals(2, taxa.totalConfirmados());
		assertEquals(1, taxa.totalNaoConfirmados());
	}

	private Interacao interacao(TipoInteracao tipo) {
		return umaInteracao(idDaInteracao(5), PACIENTE_ID, "Losartana", tipo);
	}
}
