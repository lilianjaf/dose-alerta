package com.dosealerta.ia.core.rules.interpretaraudio;

import com.dosealerta.ia.core.exception.AudioInvalidoException;

public class InterpretarAudioNaoDeveExcederTamanhoMaximoRule implements ValidadorInterpretacaoAudioRule {

	private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;
	private static final String MENSAGEM_AUDIO_GRANDE = "Áudio excede o tamanho máximo de 10MB";

	@Override
	public void validar(InterpretacaoAudioContext context) {
		if (context.audio().length > TAMANHO_MAXIMO_BYTES) {
			throw new AudioInvalidoException(MENSAGEM_AUDIO_GRANDE);
		}
	}
}
