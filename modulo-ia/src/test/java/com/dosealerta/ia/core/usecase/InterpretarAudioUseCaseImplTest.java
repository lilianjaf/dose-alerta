package com.dosealerta.ia.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.dto.IntencaoAudio;
import com.dosealerta.ia.core.exception.AudioInvalidoException;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import com.dosealerta.ia.core.rules.interpretaraudio.InterpretacaoAudioContext;
import com.dosealerta.ia.core.rules.interpretaraudio.ValidadorInterpretacaoAudioRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class InterpretarAudioUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private InterpretadorAudioGateway interpretadorAudioGateway;

	@Mock
	private ValidadorInterpretacaoAudioRule regra;

	private InterpretarAudioUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new InterpretarAudioUseCaseImpl(interpretadorAudioGateway, List.of(regra));
	}

	@Test
	void deveRepassarOAudioParaOGatewayEDevolverAInterpretacao() {
		byte[] audio = {1, 2, 3};
		AudioInterpretadoOutput esperado = new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null);
		when(interpretadorAudioGateway.interpretar(audio, "audio/ogg")).thenReturn(esperado);

		assertEquals(esperado, useCase.executar(audio, "audio/ogg"));
	}

	@Test
	void deveValidarOAudioAntesDeChamarOGateway() {
		byte[] audio = {1, 2, 3};
		when(interpretadorAudioGateway.interpretar(audio, "audio/ogg"))
				.thenReturn(new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null));

		useCase.executar(audio, "audio/ogg");

		verify(regra).validar(new InterpretacaoAudioContext(audio, "audio/ogg"));
	}

	@Test
	void naoDeveChamarOGatewayQuandoUmaRegraFalha() {
		doThrow(new AudioInvalidoException("invalido")).when(regra).validar(any());

		assertThrows(AudioInvalidoException.class, () -> useCase.executar(new byte[0], "audio/ogg"));

		verifyNoInteractions(interpretadorAudioGateway);
	}
}
