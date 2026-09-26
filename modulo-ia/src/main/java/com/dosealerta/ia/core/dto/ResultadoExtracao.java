package com.dosealerta.ia.core.dto;

import com.dosealerta.ia.core.domain.Receita;
import java.util.List;

public record ResultadoExtracao(List<Receita> receitas, List<MedicamentoNaoProcessado> naoProcessados) {
}
