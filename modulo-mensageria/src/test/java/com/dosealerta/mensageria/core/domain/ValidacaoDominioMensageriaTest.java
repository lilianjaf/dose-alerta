package com.dosealerta.mensageria.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ValidacaoDominioMensageriaTest {

	@Test
	void contatoWhatsAppRejeitaTelefoneForaDoFormatoE164() {
		assertThrows(IllegalArgumentException.class, () -> new ContatoWhatsApp(null));
		assertThrows(IllegalArgumentException.class, () -> new ContatoWhatsApp("11999999999"));
		assertEquals("+5511999999999", new ContatoWhatsApp("+5511999999999").telefone());
	}

	@Test
	void textoRejeitaConteudoVazio() {
		assertThrows(IllegalArgumentException.class, () -> new ConteudoMensagem.Texto(null));
		assertThrows(IllegalArgumentException.class, () -> new ConteudoMensagem.Texto("  "));
	}

	@Test
	void comBotaoConfirmacaoRejeitaTextoOuBotaoVazios() {
		assertThrows(IllegalArgumentException.class, () -> new ConteudoMensagem.ComBotaoConfirmacao("", "Tomei"));
		assertThrows(IllegalArgumentException.class, () -> new ConteudoMensagem.ComBotaoConfirmacao("Oi", " "));
	}

	@Test
	void templateRejeitaContentSidVazioETrataVariaveisNulasComoVazias() {
		assertThrows(IllegalArgumentException.class, () -> new ConteudoMensagem.Template(null, Map.of()));
		assertEquals(Map.of(), new ConteudoMensagem.Template("HX123", null).variaveis());
	}

	@Test
	void solicitacaoLigacaoRejeitaTextoVazio() {
		assertThrows(IllegalArgumentException.class, () -> new SolicitacaoLigacao(" "));
	}
}
