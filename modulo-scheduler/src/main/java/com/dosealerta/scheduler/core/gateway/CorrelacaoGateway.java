package com.dosealerta.scheduler.core.gateway;

public interface CorrelacaoGateway {

	String atual();

	void executarCom(String correlationId, Runnable acao);
}
