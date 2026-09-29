package com.dosealerta.notificacao.core.rules.solicitarenvio;

import com.dosealerta.notificacao.core.exception.DoseObrigatoriaException;

public class SolicitarEnvioDoseDevePreenchidaRule implements ValidadorSolicitacaoEnvioRule {

	@Override
	public void validar(SolicitacaoEnvioContext context) {
		if (context.input().dose() == null || context.input().dose().isBlank()) {
			throw new DoseObrigatoriaException();
		}
	}
}
