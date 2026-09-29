package com.dosealerta.ia.infra.controller;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.exception.AudioInvalidoException;
import com.dosealerta.ia.core.usecase.InterpretarAudioUseCase;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AudioController {

	private static final String MENSAGEM_FALHA_LEITURA = "Falha ao ler o áudio enviado";

	private final InterpretarAudioUseCase interpretarAudioUseCase;

	public AudioController(InterpretarAudioUseCase interpretarAudioUseCase) {
		this.interpretarAudioUseCase = interpretarAudioUseCase;
	}

	@PostMapping(value = "/audio/interpretar", consumes = "multipart/form-data")
	public AudioInterpretadoOutput interpretar(@RequestParam("audio") MultipartFile audio) {
		return interpretarAudioUseCase.executar(lerBytes(audio), audio.getContentType());
	}

	private byte[] lerBytes(MultipartFile audio) {
		try {
			return audio.getBytes();
		} catch (IOException e) {
			throw new AudioInvalidoException(MENSAGEM_FALHA_LEITURA);
		}
	}
}
