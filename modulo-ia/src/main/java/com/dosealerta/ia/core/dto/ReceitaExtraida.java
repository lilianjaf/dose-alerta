package com.dosealerta.ia.core.dto;

/**
 * Saída bruta do {@code ExtratorReceitaGateway}, antes do guardrail (Etapa 7.2). Tipo puro de
 * domínio — a forma como a IA é chamada (JSON estrito, prompt, SDK) é responsabilidade do
 * adapter em {@code infra.client}, que mapeia sua própria representação para este DTO.
 */
public record ReceitaExtraida(String medicamento, String dose, int frequenciaHoras, int duracaoDias) {
}
