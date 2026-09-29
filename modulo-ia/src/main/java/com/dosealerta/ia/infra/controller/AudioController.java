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

	private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;

	private final InterpretarAudioUseCase interpretarAudioUseCase;

	public AudioController(InterpretarAudioUseCase interpretarAudioUseCase) {
		this.interpretarAudioUseCase = interpretarAudioUseCase;
	}

	@PostMapping(value = "/audio/interpretar", consumes = "multipart/form-data")
	public AudioInterpretadoOutput interpretar(@RequestParam("audio") MultipartFile audio) {
		return interpretarAudioUseCase.executar(lerBytes(validar(audio)), audio.getContentType());
	}

	private MultipartFile validar(MultipartFile audio) {
		if (audio == null || audio.isEmpty()) {
			throw new AudioInvalidoException("Áudio não pode ser vazio");
		}
		if (audio.getSize() > TAMANHO_MAXIMO_BYTES) {
			throw new AudioInvalidoException("Áudio excede o tamanho máximo de 10MB");
		}
		return audio;
	}

	private byte[] lerBytes(MultipartFile audio) {
		try {
			return audio.getBytes();
		} catch (IOException e) {
			throw new AudioInvalidoException("Falha ao ler o áudio enviado");
		}
	}
}
