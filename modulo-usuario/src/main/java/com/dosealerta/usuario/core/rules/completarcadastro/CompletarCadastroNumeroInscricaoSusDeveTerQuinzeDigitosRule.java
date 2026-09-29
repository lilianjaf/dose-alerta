package com.dosealerta.usuario.core.rules.completarcadastro;

import com.dosealerta.usuario.core.exception.NumeroInscricaoSusFormatoInvalidoException;
import java.util.regex.Pattern;

public class CompletarCadastroNumeroInscricaoSusDeveTerQuinzeDigitosRule implements ValidadorCompletarCadastroRule {

	private static final Pattern PADRAO_NUMERO = Pattern.compile("^[0-9]{15}$");

	@Override
	public void validar(CompletarCadastroContext context) {
		String numero = context.input().numeroInscricaoSus();
		if (numero != null && !PADRAO_NUMERO.matcher(numero).matches()) {
			throw new NumeroInscricaoSusFormatoInvalidoException();
		}
	}
}
