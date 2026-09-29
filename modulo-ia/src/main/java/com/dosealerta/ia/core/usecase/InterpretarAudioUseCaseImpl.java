package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import com.dosealerta.ia.core.rules.interpretaraudio.InterpretacaoAudioContext;
import com.dosealerta.ia.core.rules.interpretaraudio.ValidadorInterpretacaoAudioRule;
import java.util.List;

public class InterpretarAudioUseCaseImpl implements InterpretarAudioUseCase {

	private final InterpretadorAudioGateway interpretadorAudioGateway;
	private final List<ValidadorInterpretacaoAudioRule> rules;

	public InterpretarAudioUseCaseImpl(
			InterpretadorAudioGateway interpretadorAudioGateway, List<ValidadorInterpretacaoAudioRule> rules) {
		this.interpretadorAudioGateway = interpretadorAudioGateway;
		this.rules = rules;
	}

	@Override
	public AudioInterpretadoOutput executar(byte[] audio, String tipoConteudo) {
		InterpretacaoAudioContext context = new InterpretacaoAudioContext(audio, tipoConteudo);
		rules.forEach(rule -> rule.validar(context));
		return interpretadorAudioGateway.interpretar(audio, tipoConteudo);
	}
}
