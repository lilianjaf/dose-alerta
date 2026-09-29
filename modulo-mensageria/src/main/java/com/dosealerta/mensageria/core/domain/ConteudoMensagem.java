package com.dosealerta.mensageria.core.domain;

import java.util.Map;

public sealed interface ConteudoMensagem {

	String MENSAGEM_TEXTO_VAZIO = "Texto da mensagem não pode ser vazio";
	String MENSAGEM_BOTAO_VAZIO = "Texto do botão não pode ser vazio";
	String MENSAGEM_CONTENT_SID_VAZIO = "contentSid do template não pode ser vazio";

	record Texto(String texto) implements ConteudoMensagem {
		public Texto {
			if (texto == null || texto.isBlank()) {
				throw new IllegalArgumentException(MENSAGEM_TEXTO_VAZIO);
			}
		}
	}

	record ComBotaoConfirmacao(String texto, String textoBotao) implements ConteudoMensagem {
		public ComBotaoConfirmacao {
			if (texto == null || texto.isBlank()) {
				throw new IllegalArgumentException(MENSAGEM_TEXTO_VAZIO);
			}
			if (textoBotao == null || textoBotao.isBlank()) {
				throw new IllegalArgumentException(MENSAGEM_BOTAO_VAZIO);
			}
		}
	}

	record Template(String contentSid, Map<String, String> variaveis) implements ConteudoMensagem {
		public Template {
			if (contentSid == null || contentSid.isBlank()) {
				throw new IllegalArgumentException(MENSAGEM_CONTENT_SID_VAZIO);
			}
			variaveis = variaveis == null ? Map.of() : Map.copyOf(variaveis);
		}
	}
}
