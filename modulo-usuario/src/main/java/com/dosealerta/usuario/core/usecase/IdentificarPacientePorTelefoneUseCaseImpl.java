package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.rules.identificacao.IdentificacaoPacienteContext;
import com.dosealerta.usuario.core.rules.identificacao.ValidadorIdentificacaoPacienteRule;
import java.time.Clock;
import java.util.List;

public class IdentificarPacientePorTelefoneUseCaseImpl implements IdentificarPacientePorTelefoneUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final CadastroSusGateway cadastroSusGateway;
	private final Clock clock;
	private final List<ValidadorIdentificacaoPacienteRule> rules;

	public IdentificarPacientePorTelefoneUseCaseImpl(PacienteRepositoryGateway pacienteRepositoryGateway, CadastroSusGateway cadastroSusGateway, Clock clock, List<ValidadorIdentificacaoPacienteRule> rules) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.cadastroSusGateway = cadastroSusGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public IdentificarPacienteOutput executar(IdentificarPacienteInput input) {
		rules.forEach(rule -> rule.validar(new IdentificacaoPacienteContext(input)));

		String telefone = input.telefone();
		return pacienteRepositoryGateway
				.buscarPorTelefone(telefone)
				.map(IdentificarPacienteOutput::deExistente)
				.orElseGet(() -> criar(telefone));
	}

	private IdentificarPacienteOutput criar(String telefone) {
		String nome = cadastroSusGateway.buscarNomePorTelefone(telefone).orElse(null);
		Paciente paciente = Paciente.novo(nome, telefone, null, clock.instant());
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
