package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;

public class CadastrarPacienteUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final SenhaGateway senhaGateway;

	public CadastrarPacienteUseCase(PacienteRepositoryGateway pacienteRepositoryGateway, SenhaGateway senhaGateway) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.senhaGateway = senhaGateway;
	}

	public Paciente executar(CadastrarPacienteInput input) {
		if (pacienteRepositoryGateway.buscarPorTelefone(input.telefone()).isPresent()) {
			throw new TelefoneJaCadastradoException(input.telefone());
		}

		Paciente paciente = Paciente.novo(input.nome(), input.telefone(), senhaGateway.hash(input.senha()));
		return pacienteRepositoryGateway.salvar(paciente);
	}
}
