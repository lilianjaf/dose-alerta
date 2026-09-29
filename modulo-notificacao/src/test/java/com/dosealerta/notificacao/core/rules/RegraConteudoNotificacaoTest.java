package com.dosealerta.notificacao.core.rules;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import org.junit.jupiter.api.Test;

class RegraConteudoNotificacaoTest extends TesteUnitarioBase {

	@Test
	void textoDoLembreteInicialDeveConterMedicamentoEDose() {
		String texto = RegraConteudoNotificacao.textoMensagem(EtapaEscalonamento.LEMBRETE_INICIAL, "Losartana", "50mg");

		assertTrue(texto.contains("Losartana"));
		assertTrue(texto.contains("50mg"));

		assertTrue(!texto.contains(RegraConteudoNotificacao.TEXTO_BOTAO_CONFIRMACAO));
	}

	@Test
	void textoDeReforcoDeveSerDiferenteDoLembreteInicial() {
		String lembrete = RegraConteudoNotificacao.textoMensagem(EtapaEscalonamento.LEMBRETE_INICIAL, "Losartana", "50mg");
		String reforco = RegraConteudoNotificacao.textoMensagem(EtapaEscalonamento.REFORCO, "Losartana", "50mg");

		assertTrue(!lembrete.equals(reforco));
	}

	@Test
	void deveLancarExcecaoAoPedirTextoDeMensagemParaEtapaLigacao() {
		assertThrows(
				IllegalArgumentException.class,
				() -> RegraConteudoNotificacao.textoMensagem(EtapaEscalonamento.LIGACAO, "Losartana", "50mg"));
	}

	@Test
	void textoFaladoDeveConterMedicamentoEDose() {
		String textoFalado = RegraConteudoNotificacao.textoFalado("Losartana", "50mg");

		assertTrue(textoFalado.contains("Losartana"));
		assertTrue(textoFalado.contains("50mg"));
	}
}
