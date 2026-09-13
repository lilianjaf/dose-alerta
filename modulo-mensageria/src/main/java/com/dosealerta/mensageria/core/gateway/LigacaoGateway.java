package com.dosealerta.mensageria.core.gateway;

import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;

public interface LigacaoGateway {

	void ligarParaConfirmar(ContatoWhatsApp contato, SolicitacaoLigacao solicitacao);
}
