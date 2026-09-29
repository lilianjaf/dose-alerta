package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;

public interface InterpretarAudioUseCase {

	AudioInterpretadoOutput executar(byte[] audio, String tipoConteudo);
}
