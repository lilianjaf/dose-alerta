package com.dosealerta.notificacao.core.rules.solicitarenvio;

import com.dosealerta.notificacao.core.exception.EtapaObrigatoriaException;

public class SolicitarEnvioEtapaDeveSerInformadaRule implements ValidadorSolicitacaoEnvioRule {

	@Override
	public void validar(SolicitacaoEnvioContext context) {
		if (context.input().etapa() == null) {
			throw new EtapaObrigatoriaException();
		}
	}
}
