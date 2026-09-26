package com.dosealerta.ia.core.dto;

import java.util.List;

/**
 * Resultado da leitura de uma foto de receita: quem prescreveu (médico com CRM ou dentista com CRO) e todos os
 * medicamentos prescritos.
 */
public record ReceitaExtraida(
		boolean receitaMedica,
		String nomePrescritor,
		String registroProfissional,
		List<MedicamentoExtraido> medicamentos) {
}
