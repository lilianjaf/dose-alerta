package com.dosealerta.notificacao.core.rules.solicitarenvio;

import com.dosealerta.notificacao.core.exception.AlarmeIdObrigatorioException;

public class SolicitarEnvioAlarmeIdDeveSerInformadoRule implements ValidadorSolicitacaoEnvioRule {

	@Override
	public void validar(SolicitacaoEnvioContext context) {
		if (context.input().alarmeId() == null) {
			throw new AlarmeIdObrigatorioException();
		}
	}
}
