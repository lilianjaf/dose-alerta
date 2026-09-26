package com.dosealerta.scheduler.core.dto;

import com.dosealerta.scheduler.core.domain.Alarme;

/**
 * Resultado da criação. {@code jaExistia} indica que já havia um alarme pendente do mesmo medicamento para o
 * paciente, devolvido no lugar de um novo.
 */
public record ResultadoCriarAlarme(Alarme alarme, boolean jaExistia) {
}
