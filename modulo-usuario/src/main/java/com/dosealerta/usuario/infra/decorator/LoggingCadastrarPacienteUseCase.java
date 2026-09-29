package com.dosealerta.usuario.infra.decorator;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingCadastrarPacienteUseCase implements CadastrarPacienteUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingCadastrarPacienteUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando CadastrarPaciente";
	private static final String MENSAGEM_SUCESSO = "CadastrarPaciente concluído";
	private static final String MENSAGEM_FALHA = "CadastrarPaciente falhou: {}";

	private final CadastrarPacienteUseCase delegate;

	public LoggingCadastrarPacienteUseCase(CadastrarPacienteUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public Paciente executar(CadastrarPacienteInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			Paciente resultado = delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
