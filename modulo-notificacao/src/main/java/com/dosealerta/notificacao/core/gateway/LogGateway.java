package com.dosealerta.notificacao.core.gateway;

public interface LogGateway {

	void aviso(String mensagem, Object... argumentos);

	void erro(String mensagem, Object... argumentos);
}
