package com.dosealerta.mensageria.core.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegraRespostaPacienteTest {

	@ParameterizedTest
	@ValueSource(strings = {"CONFIRMAR", "confirmar", "Confirmar", " confirmar ", "confirmar!", "confirmar.", "confirmar?"})
	void deveReconhecerConfirmarEmQualquerCaixaOuComPontuacaoNasPontas(String corpo) {
		assertTrue(RegraRespostaPaciente.ehConfirmacao(corpo, null));
	}

	@Test
	void deveReconhecerPeloTextoDoBotaoQuandoOCorpoEstaAusente() {
		assertTrue(RegraRespostaPaciente.ehConfirmacao(null, "Confirmar"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"não, confirmar está errado", "confirmo", "sim", "ok confirmar"})
	void naoDeveReconhecerQuandoTemOutraPalavraJunto(String corpo) {
		// Ainda exige que a palavra seja exatamente "confirmar" (só tirando pontuação nas pontas); não é um
		// match por substring, que poderia confundir com uma negação como "não confirmar".
		assertFalse(RegraRespostaPaciente.ehConfirmacao(corpo, null));
	}

	@Test
	void naoDeveReconhecerQuandoNaoHaCorpoNemBotao() {
		assertFalse(RegraRespostaPaciente.ehConfirmacao(null, null));
	}
}
