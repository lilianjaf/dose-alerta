package com.dosealerta.scheduler.core.rules.registrarconfirmacao;

import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;

public class RegistrarConfirmacaoAlarmePendenteDeveExistirRule implements ValidadorRegistroConfirmacaoRule {

	@Override
	public void validar(RegistroConfirmacaoContext context) {
		if (context.alarme() == null) {
			throw new AlarmePendenteNaoEncontradoException(context.telefone());
		}
	}
}
