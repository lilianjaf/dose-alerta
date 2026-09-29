package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.exception.TelefoneFormatoInvalidoException;
import java.util.regex.Pattern;

public class CompletarCadastroTelefoneDeveTerFormatoValidoRule implements ValidadorCompletarCadastroRule {

	private static final Pattern PADRAO_TELEFONE = Pattern.compile("^\\+?[0-9]{10,15}$");

	@Override
	public void validar(CompletarCadastroContext context) {
		String telefone = context.input().telefone();
		if (telefone != null && !PADRAO_TELEFONE.matcher(telefone).matches()) {
			throw new TelefoneFormatoInvalidoException();
		}
	}
}
