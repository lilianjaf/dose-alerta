package com.dosealerta.ia.core.dto;

public record AudioInterpretadoOutput(IntencaoAudio intencao, String dose, Integer frequenciaHoras, Integer duracaoDias) {

	public static AudioInterpretadoOutput naoEntendido() {
		return new AudioInterpretadoOutput(IntencaoAudio.NAO_ENTENDIDO, null, null, null);
	}
}
