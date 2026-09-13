package com.dosealerta.mensageria.core.gateway;

import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;

public interface MensageriaGateway {

	void enviarMensagem(ContatoWhatsApp contato, ConteudoMensagem conteudo);
}
