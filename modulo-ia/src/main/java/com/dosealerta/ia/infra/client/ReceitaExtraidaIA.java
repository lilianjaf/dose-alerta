package com.dosealerta.ia.infra.client;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Forma JSON estrita pedida ao modelo de visão (Etapa 7.1/7.5 do resumo técnico) — as
 * anotações Jackson aqui derivam o schema enviado no {@code output_config} da chamada.
 * Fica em {@code infra.client} (não em {@code core}) porque é um detalhe do provedor de IA,
 * não do domínio; o adapter mapeia esta classe para {@code core.dto.ReceitaExtraida}.
 */
@JsonClassDescription("Dados estruturados extraídos de uma foto de receita médica")
class ReceitaExtraidaIA {

	@JsonPropertyDescription("Nome do medicamento prescrito, exatamente como escrito na receita")
	public String medicamento;

	@JsonPropertyDescription("Dose de cada administração, incluindo a unidade, ex: '50mg', '1 comprimido'")
	public String dose;

	@JsonPropertyDescription("Intervalo entre as doses, em horas, ex: 8 para 'de 8 em 8 horas', 24 para 'uma vez ao dia'")
	public int frequenciaHoras;

	@JsonPropertyDescription("Duração total do tratamento, em dias, conforme prescrito")
	public int duracaoDias;
}
