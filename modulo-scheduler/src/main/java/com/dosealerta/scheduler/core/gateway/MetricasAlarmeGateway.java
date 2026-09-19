package com.dosealerta.scheduler.core.gateway;

/**
 * Taxa de confirmação de alarme (Etapa 9.3 do RESUMO_TECNICO.md, seção 7). Porta separada
 * (em vez de Micrometer usado direto no usecase) porque métricas são um detalhe de infra —
 * o core só sabe que precisa registrar o desfecho, não como ele vira número no Prometheus.
 */
public interface MetricasAlarmeGateway {

	void registrarConfirmacao();

	void registrarNaoConfirmacao();
}
