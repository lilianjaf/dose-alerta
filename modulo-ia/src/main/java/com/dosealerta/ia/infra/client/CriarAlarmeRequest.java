package com.dosealerta.ia.infra.client;

import java.time.Instant;
import java.util.UUID;

record CriarAlarmeRequest(UUID pacienteId, String telefone, String medicamento, String dose, Instant horarioAlvo) {
}
