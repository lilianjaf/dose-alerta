package com.dosealerta.scheduler.infra.client;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import java.util.UUID;

record SolicitarEnvioRequest(UUID alarmeId, UUID pacienteId, String medicamento, String dose, EtapaEscalonamento etapa) {

	static SolicitarEnvioRequest de(Alarme alarme, EtapaEscalonamento etapa) {
		return new SolicitarEnvioRequest(
				alarme.getId(), alarme.getPacienteId(), alarme.getMedicamento(), alarme.getDose(), etapa);
	}
}
