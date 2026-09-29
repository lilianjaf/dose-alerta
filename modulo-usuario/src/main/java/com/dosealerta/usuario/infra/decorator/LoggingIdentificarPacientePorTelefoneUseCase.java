package com.dosealerta.usuario.infra.decorator;

import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.usecase.IdentificarPacientePorTelefoneUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingIdentificarPacientePorTelefoneUseCase implements IdentificarPacientePorTelefoneUseCase {

	private static final Logger log = LoggerFactory.getLogger(LoggingIdentificarPacientePorTelefoneUseCase.class);
	private static final String MENSAGEM_INICIO = "Executando IdentificarPacientePorTelefone";
	private static final String MENSAGEM_SUCESSO = "IdentificarPacientePorTelefone concluído";
	private static final String MENSAGEM_FALHA = "IdentificarPacientePorTelefone falhou: {}";

	private final IdentificarPacientePorTelefoneUseCase delegate;

	public LoggingIdentificarPacientePorTelefoneUseCase(IdentificarPacientePorTelefoneUseCase delegate) {
		this.delegate = delegate;
	}

	@Override
	public IdentificarPacienteOutput executar(IdentificarPacienteInput input) {
		log.info(MENSAGEM_INICIO);
		try {
			IdentificarPacienteOutput resultado = delegate.executar(input);
			log.info(MENSAGEM_SUCESSO);
			return resultado;
		} catch (RuntimeException e) {
			log.warn(MENSAGEM_FALHA, e.getClass().getSimpleName());
			throw e;
		}
	}
}
