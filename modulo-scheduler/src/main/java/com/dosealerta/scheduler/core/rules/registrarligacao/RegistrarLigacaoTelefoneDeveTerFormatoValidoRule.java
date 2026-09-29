package com.dosealerta.scheduler.core.rules.registrarligacao;

import com.dosealerta.scheduler.core.exception.TelefoneFormatoInvalidoException;
import java.util.regex.Pattern;

public class RegistrarLigacaoTelefoneDeveTerFormatoValidoRule implements ValidadorRegistroLigacaoRule {

	private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+[0-9]{10,15}$");

	@Override
	public void validar(RegistroLigacaoContext context) {
		String telefone = context.telefone();
		if (telefone != null && !PADRAO_TELEFONE.matcher(telefone).matches()) {
			throw new TelefoneFormatoInvalidoException();
		}
	}
}
