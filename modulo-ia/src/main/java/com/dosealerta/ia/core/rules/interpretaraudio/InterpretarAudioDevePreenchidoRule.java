package com.dosealerta.ia.core.rules.interpretaraudio;

import com.dosealerta.ia.core.exception.AudioInvalidoException;

public class InterpretarAudioDevePreenchidoRule implements ValidadorInterpretacaoAudioRule {

	private static final String MENSAGEM_AUDIO_VAZIO = "Áudio não pode ser vazio";

	@Override
	public void validar(InterpretacaoAudioContext context) {
		byte[] audio = context.audio();
		if (audio == null || audio.length == 0) {
			throw new AudioInvalidoException(MENSAGEM_AUDIO_VAZIO);
		}
	}
}
