package com.dosealerta.ia.core.rules.buscar;

import com.dosealerta.ia.core.domain.Receita;
import java.util.UUID;

public record BuscaReceitaContext(UUID id, Receita receita) {
}
