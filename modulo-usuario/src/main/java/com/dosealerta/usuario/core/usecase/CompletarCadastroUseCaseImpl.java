package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroContext;
import com.dosealerta.usuario.core.rules.completarcadastro.ValidadorCompletarCadastroRule;
import java.util.List;

public class CompletarCadastroUseCaseImpl implements CompletarCadastroUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final CadastroSusGateway cadastroSusGateway;
	private final List<ValidadorCompletarCadastroRule> rules;

	public CompletarCadastroUseCaseImpl(PacienteRepositoryGateway pacienteRepositoryGateway, CadastroSusGateway cadastroSusGateway, List<ValidadorCompletarCadastroRule> rules) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.cadastroSusGateway = cadastroSusGateway;
		this.rules = rules;
	}

	@Override
	public Paciente executar(CompletarCadastroInput input) {
		Paciente existente = buscarPaciente(input);
		String nomeNoSus = buscarNomeNoSus(input);
		CompletarCadastroContext context = new CompletarCadastroContext(input, existente, nomeNoSus);
		rules.forEach(rule -> rule.validar(context));

		Paciente completo = Paciente.existente(
				existente.getId(), nomeNoSus, existente.getTelefone(), existente.getSenhaHash(), existente.getCriadoEm());
		return pacienteRepositoryGateway.salvar(completo);
	}

	private Paciente buscarPaciente(CompletarCadastroInput input) {
		String telefone = input.telefone();
		if (telefone == null) {
			return null;
		}
		return pacienteRepositoryGateway.buscarPorTelefone(telefone).orElse(null);
	}

	private String buscarNomeNoSus(CompletarCadastroInput input) {
		String numeroInscricaoSus = input.numeroInscricaoSus();
		if (numeroInscricaoSus == null) {
			return null;
		}
		return cadastroSusGateway.buscarNomePorNumeroInscricao(numeroInscricaoSus).orElse(null);
	}
}
