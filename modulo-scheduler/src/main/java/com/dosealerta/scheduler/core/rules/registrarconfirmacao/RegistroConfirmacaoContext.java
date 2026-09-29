package com.dosealerta.scheduler.core.rules.registrarconfirmacao;

import com.dosealerta.scheduler.core.domain.Alarme;

public record RegistroConfirmacaoContext(String telefone, Alarme alarme) {
}
