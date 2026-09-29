package com.dosealerta.ia.core.rules.extrair;

import com.dosealerta.ia.core.exception.TelefoneFormatoInvalidoException;
import java.util.regex.Pattern;

public class ExtrairTelefoneDeveTerFormatoValidoRule implements ValidadorExtracaoReceitaRule {

	private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+[0-9]{10,15}$");

	@Override
	public void validar(ExtracaoReceitaContext context) {
		String telefone = context.input().telefone();
		if (telefone != null && !PADRAO_TELEFONE.matcher(telefone).matches()) {
			throw new TelefoneFormatoInvalidoException();
		}
	}
}
