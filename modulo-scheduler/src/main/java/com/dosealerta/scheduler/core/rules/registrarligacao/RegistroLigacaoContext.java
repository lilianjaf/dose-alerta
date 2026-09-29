package com.dosealerta.scheduler.core.rules.registrarligacao;

import com.dosealerta.scheduler.core.domain.Alarme;

public record RegistroLigacaoContext(String telefone, Alarme alarme) {
}
