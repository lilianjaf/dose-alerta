package com.dosealerta.ia.core.rules.interpretaraudio;

import com.dosealerta.ia.core.exception.AudioInvalidoException;

public class InterpretarAudioTipoDeveSerAudioRule implements ValidadorInterpretacaoAudioRule {

	private static final String PREFIXO_AUDIO = "audio/";
	private static final String MENSAGEM_TIPO_INVALIDO = "O arquivo enviado não é um áudio";

	@Override
	public void validar(InterpretacaoAudioContext context) {
		String tipo = context.tipoConteudo();
		if (tipo != null && !tipo.strip().toLowerCase().startsWith(PREFIXO_AUDIO)) {
			throw new AudioInvalidoException(MENSAGEM_TIPO_INVALIDO);
		}
	}
}
