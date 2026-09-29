package com.dosealerta.notificacao.core.rules.solicitarenvio;

import com.dosealerta.notificacao.core.exception.MedicamentoObrigatorioException;

public class SolicitarEnvioMedicamentoDevePreenchidoRule implements ValidadorSolicitacaoEnvioRule {

	@Override
	public void validar(SolicitacaoEnvioContext context) {
		if (context.input().medicamento() == null || context.input().medicamento().isBlank()) {
			throw new MedicamentoObrigatorioException();
		}
	}
}
