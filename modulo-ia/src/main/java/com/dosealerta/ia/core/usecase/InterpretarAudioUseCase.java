package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;

public class InterpretarAudioUseCase {

	private final InterpretadorAudioGateway interpretadorAudioGateway;

	public InterpretarAudioUseCase(InterpretadorAudioGateway interpretadorAudioGateway) {
		this.interpretadorAudioGateway = interpretadorAudioGateway;
	}

	public AudioInterpretadoOutput executar(byte[] audio, String tipoConteudo) {
		return interpretadorAudioGateway.interpretar(audio, tipoConteudo);
	}
}
