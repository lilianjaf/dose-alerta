package com.dosealerta.usuario.infra.decorator;

import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.TokenOutput;
import com.dosealerta.usuario.core.usecase.AutenticarPacienteUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAutenticarPacienteUseCase implements AutenticarPacienteUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingAutenticarPacienteUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando AutenticarPaciente";
	private static final String MENSAGEM_SUCESSO = "AutenticarPaciente concluído";
	private static final String MENSAGEM_FALHA = "AutenticarPaciente falhou: {}";

	private final AutenticarPacienteUseCase delegate;

	public LoggingAutenticarPacienteUseCase(AutenticarPacienteUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public TokenOutput executar(AutenticarPacienteInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			TokenOutput resultado = delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
