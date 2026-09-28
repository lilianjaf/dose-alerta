package com.dosealerta.mensageria.core.rules;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
	void deveMontarOResumoDaExtracaoComReceitasCompletasEPendentesENaoProcessados() {
		String resumo = RegraMensagemReceita.resumoExtracao(1, 1, List.of("Triancil"));

		assertTrue(resumo.contains("CONFIRMAR"));
		assertTrue(resumo.toLowerCase().contains("faltaram"));
		assertTrue(resumo.contains("Triancil"));
	}

	@Test
	void deveMontarOResumoSemMencionarOQueNaoSeAplica() {
		String resumo = RegraMensagemReceita.resumoExtracao(1, 0, List.of());

		assertTrue(resumo.contains("CONFIRMAR"));
		assertTrue(resumo.isBlank() || !resumo.toLowerCase().contains("não consegui ler"));
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
}
