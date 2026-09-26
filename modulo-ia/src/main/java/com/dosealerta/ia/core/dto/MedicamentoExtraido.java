package com.dosealerta.ia.core.dto;

/**
 * Um medicamento lido da receita. Dose, frequência e duração são nulas quando a receita não as informa (ou
 * não são legíveis): o dado não é inventado, o paciente informa na confirmação.
 */
public record MedicamentoExtraido(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
}
