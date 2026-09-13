package com.dosealerta.mensageria.infra.messaging;

import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class MensagemFormatter {

	private final ObjectMapper objectMapper;

	public MensagemFormatter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public MensagemFormatada formatar(ConteudoMensagem conteudo) {
		return switch (conteudo) {
			case ConteudoMensagem.Texto texto -> MensagemFormatada.deTexto(texto.texto());
			case ConteudoMensagem.ComBotaoConfirmacao comBotao -> MensagemFormatada.deTexto(
					comBotao.texto() + "\n\nResponda *" + comBotao.textoBotao() + "* para confirmar.");
			case ConteudoMensagem.Template template -> MensagemFormatada.deTemplate(
					template.contentSid(), objectMapper.writeValueAsString(template.variaveis()));
		};
	}
}
