package com.dosealerta.mensageria.core.gateway;

public interface AlarmeClientGateway {

	void registrarConfirmacao(String telefone);

	void registrarLigacaoAtendida(String telefone);
}
