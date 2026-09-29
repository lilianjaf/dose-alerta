package com.dosealerta.relatorioadesao.core.rules.consultartaxa;

import java.time.Instant;
import java.util.UUID;

public record ConsultaTaxaAdesaoContext(UUID pacienteId, Instant inicio, Instant fim) {
}
