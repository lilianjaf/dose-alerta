package com.dosealerta.ia.core.rules.receitaextraida;

import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;

public class ReceitaExtraidaDeveTerRegistroProfissionalPlausivelRule implements ValidadorReceitaExtraidaRule {

	private static final String MOTIVO = "registro profissional (CRM/CRO) não identificado";
	private static final int REGISTRO_MIN_DIGITOS = 4;
	private static final int REGISTRO_MAX_DIGITOS = 7;

	@Override
	public void validar(ReceitaExtraidaContext context) {
		String registro = context.extraida().registroProfissional();
		if (!registroPlausivel(registro)) {
			throw new ReceitaFormalNaoIdentificadaException(MOTIVO);
		}
	}

	private boolean registroPlausivel(String registro) {
		if (registro == null) {
			return false;
		}
		long digitos = registro.chars().filter(Character::isDigit).count();
		return digitos >= REGISTRO_MIN_DIGITOS && digitos <= REGISTRO_MAX_DIGITOS;
	}
}
