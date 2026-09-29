package com.dosealerta.scheduler.core.rules.buscar;

import com.dosealerta.scheduler.core.domain.Alarme;
import java.util.UUID;

public record BuscaAlarmeContext(UUID id, Alarme alarme) {
}
