package com.dosealerta.relatorioadesao.core.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dosealerta.relatorioadesao.core.domain.Interacao;
import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import com.dosealerta.relatorioadesao.core.domain.TipoInteracao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegraCalculoTaxaAdesaoTest {

	private static final UUID PACIENTE_ID = UUID.randomUUID();
	private static final Instant AGORA = Instant.now();

	@Test
	void deveCalcularTaxaComBaseApenasEmConfirmacoesENaoConfirmacoes() {
		List<Interacao> interacoes = List.of(
				interacao(TipoInteracao.CONFIRMACAO),
				interacao(TipoInteracao.CONFIRMACAO),
				interacao(TipoInteracao.CONFIRMACAO),
				interacao(TipoInteracao.NAO_CONFIRMACAO));

		TaxaAdesao taxa = RegraCalculoTaxaAdesao.calcular("Losartana", interacoes, AGORA, AGORA);

		assertEquals(3, taxa.totalConfirmados());
		assertEquals(1, taxa.totalNaoConfirmados());
		assertEquals(0.75, taxa.taxaConfirmacao());
	}

	@Test
	void naoDeveContarLigacaoAtendidaNoDenominador() {
		List<Interacao> interacoes = List.of(
				interacao(TipoInteracao.CONFIRMACAO), interacao(TipoInteracao.LIGACAO_ATENDIDA),
				interacao(TipoInteracao.LIGACAO_ATENDIDA));

		TaxaAdesao taxa = RegraCalculoTaxaAdesao.calcular("Losartana", interacoes, AGORA, AGORA);

		assertEquals(1, taxa.totalConfirmados());
		assertEquals(0, taxa.totalNaoConfirmados());
		assertEquals(2, taxa.totalLigacoesAtendidas());
		assertEquals(1.0, taxa.taxaConfirmacao());
	}

	@Test
	void deveRetornarTaxaNulaQuandoNaoHaDesfechoNoPeriodo() {
		List<Interacao> interacoes = List.of(interacao(TipoInteracao.LIGACAO_ATENDIDA));

		TaxaAdesao taxa = RegraCalculoTaxaAdesao.calcular("Losartana", interacoes, AGORA, AGORA);

		assertNull(taxa.taxaConfirmacao());
	}

	@Test
	void deveAgruparPorMedicamento() {
		List<Interacao> interacoes = List.of(
				new Interacao(UUID.randomUUID(), PACIENTE_ID, "Losartana", TipoInteracao.CONFIRMACAO, AGORA),
				new Interacao(UUID.randomUUID(), PACIENTE_ID, "Metformina", TipoInteracao.NAO_CONFIRMACAO, AGORA));

		List<TaxaAdesao> taxas = RegraCalculoTaxaAdesao.calcularPorMedicamento(interacoes, AGORA, AGORA);

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
				new Interacao(UUID.randomUUID(), PACIENTE_ID, "Losartana", TipoInteracao.CONFIRMACAO, AGORA),
				new Interacao(UUID.randomUUID(), PACIENTE_ID, "losartana", TipoInteracao.CONFIRMACAO, AGORA),
				new Interacao(UUID.randomUUID(), PACIENTE_ID, " Losartana ", TipoInteracao.NAO_CONFIRMACAO, AGORA));

		List<TaxaAdesao> taxas = RegraCalculoTaxaAdesao.calcularPorMedicamento(interacoes, AGORA, AGORA);

		assertEquals(1, taxas.size());
		TaxaAdesao taxa = taxas.get(0);
		assertEquals("Losartana", taxa.medicamento());
		assertEquals(2, taxa.totalConfirmados());
		assertEquals(1, taxa.totalNaoConfirmados());
	}

	private Interacao interacao(TipoInteracao tipo) {
		return new Interacao(UUID.randomUUID(), PACIENTE_ID, "Losartana", tipo, AGORA);
	}
}
