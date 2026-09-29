package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;

public class InterpretarAudioUseCaseImpl implements InterpretarAudioUseCase {

	private final InterpretadorAudioGateway interpretadorAudioGateway;

	public InterpretarAudioUseCaseImpl(InterpretadorAudioGateway interpretadorAudioGateway) {
		this.interpretadorAudioGateway = interpretadorAudioGateway;
	}

	@Override
	public AudioInterpretadoOutput executar(byte[] audio, String tipoConteudo) {
		return interpretadorAudioGateway.interpretar(audio, tipoConteudo);
	}
}
