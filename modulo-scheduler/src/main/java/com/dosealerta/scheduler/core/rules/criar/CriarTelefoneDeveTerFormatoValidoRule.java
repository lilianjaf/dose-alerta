package com.dosealerta.scheduler.core.rules.criar;

import com.dosealerta.scheduler.core.exception.TelefoneFormatoInvalidoException;
import java.util.regex.Pattern;

public class CriarTelefoneDeveTerFormatoValidoRule implements ValidadorCriacaoAlarmeRule {

	private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+[0-9]{10,15}$");

	@Override
	public void validar(CriacaoAlarmeContext context) {
		String telefone = context.input().telefone();
		if (telefone != null && !PADRAO_TELEFONE.matcher(telefone).matches()) {
			throw new TelefoneFormatoInvalidoException();
		}
	}
}
