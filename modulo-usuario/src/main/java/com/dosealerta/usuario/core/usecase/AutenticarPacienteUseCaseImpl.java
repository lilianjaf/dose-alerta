package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.TokenOutput;
import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import com.dosealerta.usuario.core.rules.autenticacao.AutenticacaoContext;
import com.dosealerta.usuario.core.rules.autenticacao.ValidadorAutenticacaoRule;
import java.util.List;

public class AutenticarPacienteUseCaseImpl implements AutenticarPacienteUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final SenhaGateway senhaGateway;
	private final AutenticacaoGateway autenticacaoGateway;
	private final List<ValidadorAutenticacaoRule> rules;

	public AutenticarPacienteUseCaseImpl(PacienteRepositoryGateway pacienteRepositoryGateway, SenhaGateway senhaGateway, AutenticacaoGateway autenticacaoGateway, List<ValidadorAutenticacaoRule> rules) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.senhaGateway = senhaGateway;
		this.autenticacaoGateway = autenticacaoGateway;
		this.rules = rules;
	}

	@Override
	public TokenOutput executar(AutenticarPacienteInput input) {
		Paciente paciente = buscarPaciente(input);
		boolean senhaConfere = senhaConfere(input, paciente);
		AutenticacaoContext context = new AutenticacaoContext(input, paciente, senhaConfere);
		rules.forEach(rule -> rule.validar(context));

		return new TokenOutput(autenticacaoGateway.emitirToken(paciente));
	}

	private Paciente buscarPaciente(AutenticarPacienteInput input) {
		String telefone = input.telefone();
		if (telefone == null) {
			return null;
		}
		return pacienteRepositoryGateway.buscarPorTelefone(telefone).orElse(null);
	}

	private boolean senhaConfere(AutenticarPacienteInput input, Paciente paciente) {
		return paciente != null
				&& input.senha() != null
				&& senhaGateway.confere(input.senha(), paciente.getSenhaHash());
	}
}
