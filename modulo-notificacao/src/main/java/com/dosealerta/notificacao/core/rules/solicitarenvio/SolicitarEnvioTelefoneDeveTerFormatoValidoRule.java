package com.dosealerta.notificacao.core.rules.solicitarenvio;

import com.dosealerta.notificacao.core.exception.TelefoneFormatoInvalidoException;
import java.util.regex.Pattern;

public class SolicitarEnvioTelefoneDeveTerFormatoValidoRule implements ValidadorSolicitacaoEnvioRule {

	private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+[0-9]{10,15}$");

	@Override
	public void validar(SolicitacaoEnvioContext context) {
		String telefone = context.input().telefone();
		if (telefone != null && !PADRAO_TELEFONE.matcher(telefone).matches()) {
			throw new TelefoneFormatoInvalidoException();
		}
	}
}
