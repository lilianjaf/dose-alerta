package com.dosealerta.ia.core.gateway;

import java.time.Instant;
import java.util.UUID;

public interface SchedulerClientGateway {

	void criarAlarme(UUID pacienteId, String telefone, String medicamento, String dose, Instant horarioAlvo);
}
