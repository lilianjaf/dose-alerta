package com.dosealerta.ia.core.rules.confirmarportelefone;

import com.dosealerta.ia.core.domain.Receita;

public record ConfirmacaoPorTelefoneContext(String telefone, Receita pendente) {
}
