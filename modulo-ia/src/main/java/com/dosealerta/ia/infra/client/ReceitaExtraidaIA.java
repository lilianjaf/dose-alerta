package com.dosealerta.ia.infra.client;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

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
