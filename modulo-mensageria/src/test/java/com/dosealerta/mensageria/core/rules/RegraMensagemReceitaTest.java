package com.dosealerta.mensageria.core.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.mensageria.core.dto.ReceitaCriada;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegraMensagemReceitaTest {

	@Test
	void deveMontarAsMensagensDeCadastro() {
		assertTrue(RegraMensagemReceita.pedirNumeroInscricaoSus().toLowerCase().contains("inscrição"));
		assertTrue(RegraMensagemReceita.numeroInscricaoSusNaoEncontrado().toLowerCase().contains("sus"));
		assertTrue(RegraMensagemReceita.boasVindas().toLowerCase().contains("cadastro"));
		assertTrue(RegraMensagemReceita.ajuda().toLowerCase().contains("foto"));
	}

	@Test
	void deveListarDoseFrequenciaEDuracaoDosMedicamentosIdentificados() {
		List<ReceitaCriada> receitas = List.of(
				new ReceitaCriada("Losartana 50mg", "1 comprimido", 24, 30, List.of()),
				new ReceitaCriada("Amoxicilina 500mg", null, 8, null, List.of("dose", "duracaoDias")));

		String resumo = RegraMensagemReceita.resumoExtracao(receitas, List.of("Triancil"));

		assertTrue(resumo.contains("CONFIRMAR"));
		assertTrue(resumo.contains("Losartana 50mg"));
		assertTrue(resumo.contains("1 comprimido"));
		assertTrue(resumo.contains("24"));
		assertTrue(resumo.contains("30"));
		assertTrue(resumo.toLowerCase().contains("faltaram"));
		assertTrue(resumo.contains("Amoxicilina 500mg"));
		assertTrue(resumo.contains("Triancil"));
		// Direcionamento de como corrigir já vem junto do resumo, não só quando falta dado.
		assertTrue(resumo.contains(";"));
	}

	@Test
	void deveMontarOResumoSemMencionarOQueNaoSeAplica() {
		List<ReceitaCriada> receitas = List.of(new ReceitaCriada("Losartana 50mg", "1 comprimido", 24, 30, List.of()));

		String resumo = RegraMensagemReceita.resumoExtracao(receitas, List.of());

		assertTrue(resumo.contains("CONFIRMAR"));
		assertFalse(resumo.toLowerCase().contains("não consegui ler"));
		assertFalse(resumo.toLowerCase().contains("faltaram"));
	}

	@Test
	void devePedirOsCamposPendentesComNomesLegiveis() {
		String mensagem = RegraMensagemReceita.pedirCamposPendentes(List.of("dose", "duracaoDias"));

		assertTrue(mensagem.contains("dose"));
		assertTrue(mensagem.toLowerCase().contains("duração"));
		assertTrue(mensagem.contains(";"));
	}

	@Test
	void deveConfirmarComONomeDoMedicamento() {
		assertTrue(RegraMensagemReceita.confirmada("Losartana").contains("Losartana"));
	}

	@Test
	void deveOrientarOFormatoDeCorrecaoQuandoNaoEntendeAResposta() {
		String mensagem = RegraMensagemReceita.correcaoNaoEntendida();

		assertTrue(mensagem.contains(";"));
		assertTrue(mensagem.contains("CONFIRMAR"));
	}
}
