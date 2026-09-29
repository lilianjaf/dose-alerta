package com.dosealerta.mensageria.core.dto;

import java.util.UUID;

public record IdentificarPacienteResultado(UUID pacienteId, String nome, boolean cadastroCompleto, boolean recemCriado) {
}
