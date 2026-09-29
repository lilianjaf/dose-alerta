package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;

public class IdentificarPacientePorTelefoneUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final CadastroSusGateway cadastroSusGateway;

	public IdentificarPacientePorTelefoneUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway, CadastroSusGateway cadastroSusGateway) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.cadastroSusGateway = cadastroSusGateway;
	}

	public IdentificarPacienteOutput executar(IdentificarPacienteInput input) {
		String telefone = input.telefone();
		return pacienteRepositoryGateway
				.buscarPorTelefone(telefone)
				.map(IdentificarPacienteOutput::deExistente)
				.orElseGet(() -> criar(telefone));
	}

	private IdentificarPacienteOutput criar(String telefone) {
		String nome = cadastroSusGateway.buscarNomePorTelefone(telefone).orElse(null);
		Paciente paciente = Paciente.novo(nome, telefone, null);
		try {
			return IdentificarPacienteOutput.deRecemCriado(pacienteRepositoryGateway.salvar(paciente));
		} catch (TelefoneJaCadastradoException e) {

			return pacienteRepositoryGateway
					.buscarPorTelefone(telefone)
					.map(IdentificarPacienteOutput::deRecemCriado)
					.orElseThrow(() -> e);
		}
	}
}
