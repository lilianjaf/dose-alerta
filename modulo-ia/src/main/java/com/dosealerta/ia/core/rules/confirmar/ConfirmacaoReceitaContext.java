package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.domain.Receita;
import java.util.UUID;

public record ConfirmacaoReceitaContext(UUID receitaId, Receita receita, String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
}
