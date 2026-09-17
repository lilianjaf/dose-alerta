package com.dosealerta.ia.core.dto;

import java.time.Instant;
import java.util.UUID;

public record ExtrairReceitaInput(UUID pacienteId, String telefone, Instant horarioInicial, byte[] imagem) {
}
