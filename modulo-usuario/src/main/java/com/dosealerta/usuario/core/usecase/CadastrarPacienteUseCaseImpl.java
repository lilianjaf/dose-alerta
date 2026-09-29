package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import com.dosealerta.usuario.core.rules.cadastro.CadastroPacienteContext;
import com.dosealerta.usuario.core.rules.cadastro.ValidadorCadastroPacienteRule;
import java.time.Clock;
import java.util.List;

public class CadastrarPacienteUseCaseImpl implements CadastrarPacienteUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final SenhaGateway senhaGateway;
	private final Clock clock;
	private final List<ValidadorCadastroPacienteRule> rules;

	public CadastrarPacienteUseCaseImpl(PacienteRepositoryGateway pacienteRepositoryGateway, SenhaGateway senhaGateway, Clock clock, List<ValidadorCadastroPacienteRule> rules) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.senhaGateway = senhaGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public Paciente executar(CadastrarPacienteInput input) {
		boolean telefoneJaCadastrado = telefoneJaCadastrado(input);
		CadastroPacienteContext context = new CadastroPacienteContext(input, telefoneJaCadastrado);
		rules.forEach(rule -> rule.validar(context));

		Paciente paciente = Paciente.novo(input.nome(), input.telefone(), senhaGateway.hash(input.senha()), clock.instant());
		return pacienteRepositoryGateway.salvar(paciente);
	}

	private boolean telefoneJaCadastrado(CadastrarPacienteInput input) {
		String telefone = input.telefone();
		return telefone != null && pacienteRepositoryGateway.buscarPorTelefone(telefone).isPresent();
	}
}
