package com.dosealerta.mensageria.core.dto;

public record InterpretacaoAudioResultado(IntencaoAudio intencao, String dose, Integer frequenciaHoras, Integer duracaoDias) {

	public static InterpretacaoAudioResultado naoEntendido() {
		return new InterpretacaoAudioResultado(IntencaoAudio.NAO_ENTENDIDO, null, null, null);
	}

	public CorrecaoReceita paraCorrecao() {
		return new CorrecaoReceita(null, dose, frequenciaHoras, duracaoDias);
	}

	public boolean possuiCorrecao() {
		return (dose != null && !dose.isBlank()) || frequenciaHoras != null || duracaoDias != null;
	}
}
