package com.dosealerta.mensageria.core.gateway;

/**
 * Encaminha ao modulo-scheduler os sinais de resposta do paciente capturados pelos webhooks
 * do Twilio (botão/texto no WhatsApp, dígito ou atendimento da ligação). O modulo-scheduler
 * é o dono do agregado Alarme/Interacao — o modulo-mensageria apenas relata o que recebeu,
 * sem conhecer regra de escalonamento.
 */
public interface AlarmeClientGateway {

	void registrarConfirmacao(String telefone);

	void registrarLigacaoAtendida(String telefone);
}
