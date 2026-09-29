package com.dosealerta.ia.infra.decorator;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.usecase.InterpretarAudioUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingInterpretarAudioUseCase implements InterpretarAudioUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingInterpretarAudioUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando InterpretarAudio";
	private static final String MENSAGEM_SUCESSO = "InterpretarAudio concluído";
	private static final String MENSAGEM_FALHA = "InterpretarAudio falhou: {}";

	private final InterpretarAudioUseCase delegate;

	public LoggingInterpretarAudioUseCase(InterpretarAudioUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public AudioInterpretadoOutput executar(byte[] audio, String tipoConteudo) {
		log.info(MENSAGEM_INICIO);
		try {
			AudioInterpretadoOutput resultado = delegate.executar(audio, tipoConteudo);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
