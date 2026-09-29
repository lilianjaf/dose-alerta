package com.dosealerta.mensageria.core.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegraRespostaPacienteTest {

	@ParameterizedTest
	@ValueSource(
			strings = {
				"CONFIRMAR", "confirmar", "Confirmar", " confirmar ", "confirmar!", "confirmar.", "confirmar?",
				"confirmo", "CONFIRMO", "confirma", "confirmado", "tomei", "TOMEI", "tomo", "tomado"
			})
	void deveReconhecerAsConjugacoesDeConfirmarEmQualquerCaixaOuComPontuacaoNasPontas(String corpo) {
		assertTrue(RegraRespostaPaciente.ehConfirmacao(corpo, null));
	}

	@Test
	void deveReconhecerPeloTextoDoBotaoQuandoOCorpoEstaAusente() {
		assertTrue(RegraRespostaPaciente.ehConfirmacao(null, "Confirmar"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"não, confirmar está errado", "sim", "ok confirmar"})
	void naoDeveReconhecerQuandoTemOutraPalavraJunto(String corpo) {

		assertFalse(RegraRespostaPaciente.ehConfirmacao(corpo, null));
	}

	@Test
	void naoDeveReconhecerQuandoNaoHaCorpoNemBotao() {
		assertFalse(RegraRespostaPaciente.ehConfirmacao(null, null));
	}

	@ParameterizedTest
	@ValueSource(strings = {"NÃO TOMEI", "não tomei", "Não Tomei", "nao tomei", "não", "NAO", " não "})
	void deveReconhecerNegacaoEmQualquerCaixaOuAcentuacao(String corpo) {
		assertTrue(RegraRespostaPaciente.ehNegacao(corpo, null));
	}

	@Test
	void deveReconhecerNegacaoPeloTextoDoBotao() {
		assertTrue(RegraRespostaPaciente.ehNegacao(null, "Não tomei"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"tomei", "confirmar", "não sei", "ainda não tomei"})
	void naoDeveReconhecerNegacaoForaDasPalavrasExatas(String corpo) {
		assertFalse(RegraRespostaPaciente.ehNegacao(corpo, null));
	}
}
