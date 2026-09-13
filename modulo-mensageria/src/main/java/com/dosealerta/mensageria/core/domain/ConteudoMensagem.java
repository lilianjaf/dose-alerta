package com.dosealerta.mensageria.core.domain;

import java.util.Map;

public sealed interface ConteudoMensagem {

	record Texto(String texto) implements ConteudoMensagem {
		public Texto {
			if (texto == null || texto.isBlank()) {
				throw new IllegalArgumentException("Texto da mensagem não pode ser vazio");
			}
		}
	}

	record ComBotaoConfirmacao(String texto, String textoBotao) implements ConteudoMensagem {
		public ComBotaoConfirmacao {
			if (texto == null || texto.isBlank()) {
				throw new IllegalArgumentException("Texto da mensagem não pode ser vazio");
			}
			if (textoBotao == null || textoBotao.isBlank()) {
				throw new IllegalArgumentException("Texto do botão não pode ser vazio");
			}
		}
	}

	record Template(String contentSid, Map<String, String> variaveis) implements ConteudoMensagem {
		public Template {
			if (contentSid == null || contentSid.isBlank()) {
				throw new IllegalArgumentException("contentSid do template não pode ser vazio");
			}
			variaveis = variaveis == null ? Map.of() : Map.copyOf(variaveis);
		}
	}
}
