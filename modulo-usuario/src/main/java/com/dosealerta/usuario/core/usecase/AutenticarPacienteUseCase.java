package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.TokenOutput;
import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;

public class AutenticarPacienteUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final SenhaGateway senhaGateway;
	private final AutenticacaoGateway autenticacaoGateway;

	public AutenticarPacienteUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway,
			SenhaGateway senhaGateway,
			AutenticacaoGateway autenticacaoGateway) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.senhaGateway = senhaGateway;
		this.autenticacaoGateway = autenticacaoGateway;
	}

	public TokenOutput executar(AutenticarPacienteInput input) {
		Paciente paciente = pacienteRepositoryGateway.buscarPorTelefone(input.telefone())
				.orElseThrow(CredenciaisInvalidasException::new);

		if (!senhaGateway.confere(input.senha(), paciente.getSenhaHash())) {
			throw new CredenciaisInvalidasException();
		}

		return new TokenOutput(autenticacaoGateway.emitirToken(paciente));
	}
}
