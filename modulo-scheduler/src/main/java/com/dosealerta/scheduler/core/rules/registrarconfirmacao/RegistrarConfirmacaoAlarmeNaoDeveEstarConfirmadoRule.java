package com.dosealerta.scheduler.core.rules.registrarconfirmacao;

import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.exception.AlarmeJaConfirmadoException;

public class RegistrarConfirmacaoAlarmeNaoDeveEstarConfirmadoRule implements ValidadorRegistroConfirmacaoRule {

	@Override
	public void validar(RegistroConfirmacaoContext context) {
		if (context.alarme().getStatus() == StatusAlarme.CONFIRMADO) {
			throw new AlarmeJaConfirmadoException(context.alarme().getId());
		}
	}
}
