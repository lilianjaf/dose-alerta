package com.dosealerta.notificacao.core.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import org.junit.jupiter.api.Test;

class RegraEstrategiaCanalTest {

	@Test
	void deveResolverMensagemParaLembreteInicial() {
		assertEquals(Canal.MENSAGEM, RegraEstrategiaCanal.decidir(EtapaEscalonamento.LEMBRETE_INICIAL));
	}

	@Test
	void deveResolverMensagemParaReforco() {
		assertEquals(Canal.MENSAGEM, RegraEstrategiaCanal.decidir(EtapaEscalonamento.REFORCO));
	}

	@Test
	void deveResolverLigacaoParaEtapaLigacao() {
		assertEquals(Canal.LIGACAO, RegraEstrategiaCanal.decidir(EtapaEscalonamento.LIGACAO));
	}
}
