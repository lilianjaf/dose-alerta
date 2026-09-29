package com.dosealerta.ia.core.rules.confirmar;

import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.exception.ReceitaJaConfirmadaException;

public class ConfirmarReceitaDeveEstarAguardandoConfirmacaoRule implements ValidadorConfirmacaoReceitaRule {

	@Override
	public void validar(ConfirmacaoReceitaContext context) {
		if (context.receita().getStatus() == StatusReceita.CONFIRMADA) {
			throw new ReceitaJaConfirmadaException(context.receitaId());
		}
	}
}
