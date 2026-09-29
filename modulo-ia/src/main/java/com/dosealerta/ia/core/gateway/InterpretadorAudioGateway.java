package com.dosealerta.ia.core.gateway;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;

public interface InterpretadorAudioGateway {

	AudioInterpretadoOutput interpretar(byte[] audio, String tipoConteudo);
}
