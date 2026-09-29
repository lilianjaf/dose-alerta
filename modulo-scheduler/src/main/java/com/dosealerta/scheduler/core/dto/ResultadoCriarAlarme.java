package com.dosealerta.scheduler.core.dto;

import com.dosealerta.scheduler.core.domain.Alarme;

public record ResultadoCriarAlarme(Alarme alarme, boolean jaExistia) {
}
