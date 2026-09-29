package com.dosealerta.notificacao.core.rules.solicitarenvio;

import com.dosealerta.notificacao.core.exception.PacienteIdObrigatorioException;

public class SolicitarEnvioPacienteIdDeveSerInformadoRule implements ValidadorSolicitacaoEnvioRule {

	@Override
	public void validar(SolicitacaoEnvioContext context) {
		if (context.input().pacienteId() == null) {
			throw new PacienteIdObrigatorioException();
		}
	}
}
