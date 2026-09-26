package com.dosealerta.ia.core.dto;

/** Medicamento lido da receita que não pôde virar um lembrete, com o motivo. */
public record MedicamentoNaoProcessado(String medicamento, String motivo) {
}
