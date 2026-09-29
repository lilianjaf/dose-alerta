package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.dto.IntencaoAudio;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class InterpretarAudioUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private InterpretadorAudioGateway interpretadorAudioGateway;

	@Test
	void deveRepassarOAudioParaOGatewayEDevolverAInterpretacao() {
		byte[] audio = {1, 2, 3};
		AudioInterpretadoOutput esperado = new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null);
		when(interpretadorAudioGateway.interpretar(audio, "audio/ogg")).thenReturn(esperado);

		InterpretarAudioUseCase useCase = new InterpretarAudioUseCaseImpl(interpretadorAudioGateway);

		assertEquals(esperado, useCase.executar(audio, "audio/ogg"));
	}
}
