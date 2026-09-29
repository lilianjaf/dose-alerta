package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.dto.IntencaoAudio;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterpretarAudioUseCaseTest {

	@Mock
	private InterpretadorAudioGateway interpretadorAudioGateway;

	@Test
	void deveRepassarOAudioParaOGatewayEDevolverAInterpretacao() {
		byte[] audio = {1, 2, 3};
		AudioInterpretadoOutput esperado = new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null);
		when(interpretadorAudioGateway.interpretar(audio, "audio/ogg")).thenReturn(esperado);

		InterpretarAudioUseCase useCase = new InterpretarAudioUseCase(interpretadorAudioGateway);

		assertEquals(esperado, useCase.executar(audio, "audio/ogg"));
	}
}
