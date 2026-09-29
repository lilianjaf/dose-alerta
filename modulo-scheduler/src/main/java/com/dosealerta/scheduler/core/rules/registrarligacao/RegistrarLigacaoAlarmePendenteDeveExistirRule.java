package com.dosealerta.scheduler.core.rules.registrarligacao;

import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;

public class RegistrarLigacaoAlarmePendenteDeveExistirRule implements ValidadorRegistroLigacaoRule {

	@Override
	public void validar(RegistroLigacaoContext context) {
		if (context.alarme() == null) {
			throw new AlarmePendenteNaoEncontradoException(context.telefone());
		}
	}
}
