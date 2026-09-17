package com.dosealerta.ia.core.gateway;

import java.time.Instant;
import java.util.UUID;

/**
 * Aciona o modulo-scheduler para criar o alarme da primeira dose de uma receita confirmada —
 * o mesmo endpoint que já recebia extrações manuais ao final da Etapa 6.
 */
public interface SchedulerClientGateway {

	void criarAlarme(UUID pacienteId, String telefone, String medicamento, String dose, Instant horarioAlvo);
}
