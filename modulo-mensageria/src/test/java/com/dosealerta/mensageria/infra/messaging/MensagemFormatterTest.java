package com.dosealerta.mensageria.infra.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class MensagemFormatterTest {

	private final MensagemFormatter formatter = new MensagemFormatter(new ObjectMapper());

	@Test
	void deveFormatarTextoSimples() {
		var conteudo = new ConteudoMensagem.Texto("Hora de tomar Losartana");

		MensagemFormatada formatada = formatter.formatar(conteudo);

		assertEquals("Hora de tomar Losartana", formatada.corpo());
		assertFalse(formatada.isTemplate());
	}

	@Test
	void deveFormatarTextoComInstrucaoDeBotao() {
		var conteudo = new ConteudoMensagem.ComBotaoConfirmacao("Hora de tomar Losartana", "1");

		MensagemFormatada formatada = formatter.formatar(conteudo);

		assertTrue(formatada.corpo().contains("Hora de tomar Losartana"));
		assertTrue(formatada.corpo().contains("*1*"));
		assertFalse(formatada.isTemplate());
	}

	@Test
	void deveFormatarTemplateComContentSidEVariaveis() {
		var conteudo = new ConteudoMensagem.Template("HX123", Map.of("1", "Losartana"));

		MensagemFormatada formatada = formatter.formatar(conteudo);

		assertTrue(formatada.isTemplate());
		assertEquals("HX123", formatada.contentSid());
		assertEquals("{\"1\":\"Losartana\"}", formatada.contentVariablesJson());
	}
}
