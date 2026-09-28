package com.dosealerta.mensageria.infra.client;

import java.util.UUID;

record IdentificarPacienteResponse(UUID pacienteId, String nome, boolean cadastroCompleto, boolean recemCriado) {
}
