package com.dosealerta.scheduler.infra.client;

import com.dosealerta.scheduler.core.domain.TipoInteracao;
import java.time.Instant;
import java.util.UUID;

record RegistrarInteracaoRequest(
		UUID id, UUID alarmeId, UUID pacienteId, String medicamento, TipoInteracao tipo, Instant registradaEm) {
}
