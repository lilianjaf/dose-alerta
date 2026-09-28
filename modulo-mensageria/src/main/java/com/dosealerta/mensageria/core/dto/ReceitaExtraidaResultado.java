package com.dosealerta.mensageria.core.dto;

import java.util.List;

public record ReceitaExtraidaResultado(List<ReceitaCriada> receitas, List<String> naoProcessados) {
}
