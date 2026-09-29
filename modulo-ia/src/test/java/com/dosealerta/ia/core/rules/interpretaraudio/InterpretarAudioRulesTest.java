package com.dosealerta.ia.core.rules.interpretaraudio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.exception.AudioInvalidoException;
import org.junit.jupiter.api.Test;

class InterpretarAudioRulesTest extends TesteUnitarioBase {

	private static final int UM_BYTE_ACIMA_DO_LIMITE = 10 * 1024 * 1024 + 1;
	private static final byte[] AUDIO = {1, 2, 3};

	private final InterpretarAudioDevePreenchidoRule preenchidoRule = new InterpretarAudioDevePreenchidoRule();
	private final InterpretarAudioNaoDeveExcederTamanhoMaximoRule tamanhoRule =
			new InterpretarAudioNaoDeveExcederTamanhoMaximoRule();
	private final InterpretarAudioTipoDeveSerAudioRule tipoRule = new InterpretarAudioTipoDeveSerAudioRule();

	@Test
	void devePassarQuandoTudoValido() {
		InterpretacaoAudioContext contexto = new InterpretacaoAudioContext(AUDIO, "audio/ogg");

		assertDoesNotThrow(() -> preenchidoRule.validar(contexto));
		assertDoesNotThrow(() -> tamanhoRule.validar(contexto));
		assertDoesNotThrow(() -> tipoRule.validar(contexto));
	}

	@Test
	void deveRejeitarAudioNuloOuVazio() {
		assertThrows(
				AudioInvalidoException.class,
				() -> preenchidoRule.validar(new InterpretacaoAudioContext(null, "audio/ogg")));
		assertThrows(
				AudioInvalidoException.class,
				() -> preenchidoRule.validar(new InterpretacaoAudioContext(new byte[0], "audio/ogg")));
	}

	@Test
	void deveRejeitarAudioAcimaDoLimite() {
		InterpretacaoAudioContext contexto =
				new InterpretacaoAudioContext(new byte[UM_BYTE_ACIMA_DO_LIMITE], "audio/ogg");

		assertThrows(AudioInvalidoException.class, () -> tamanhoRule.validar(contexto));
	}

	@Test
	void deveRejeitarArquivoQueNaoSejaAudio() {
		assertThrows(
				AudioInvalidoException.class, () -> tipoRule.validar(new InterpretacaoAudioContext(AUDIO, "image/png")));
	}

	@Test
	void deveAceitarTipoNaoInformadoEComParametros() {
		assertDoesNotThrow(() -> tipoRule.validar(new InterpretacaoAudioContext(AUDIO, null)));
		assertDoesNotThrow(() -> tipoRule.validar(new InterpretacaoAudioContext(AUDIO, "audio/ogg; codecs=opus")));
	}
}
