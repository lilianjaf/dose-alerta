package com.dosealerta.scheduler.core.rules.registrarconfirmacao;

import com.dosealerta.scheduler.core.exception.TelefoneFormatoInvalidoException;
import java.util.regex.Pattern;

public class RegistrarConfirmacaoTelefoneDeveTerFormatoValidoRule implements ValidadorRegistroConfirmacaoRule {

	private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+[0-9]{10,15}$");

	@Override
	public void validar(RegistroConfirmacaoContext context) {
		String telefone = context.telefone();
		if (telefone != null && !PADRAO_TELEFONE.matcher(telefone).matches()) {
			throw new TelefoneFormatoInvalidoException();
		}
	}
}
