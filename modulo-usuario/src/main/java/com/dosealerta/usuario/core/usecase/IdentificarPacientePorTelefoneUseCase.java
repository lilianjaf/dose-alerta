package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;

/**
 * Primeiro contato do paciente pelo WhatsApp. Se o telefone já tem um {@link Paciente} completo, identifica
 * direto. Se é a primeira vez que qualquer sistema o vê, consulta o SUS: achou → paciente identificado sem
 * precisar de mais nada; não achou → cria um cadastro incompleto (nome nulo) para o autocadastro continuar na
 * próxima mensagem, via {@link CompletarCadastroUseCase}.
 */
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
			// Colisão rara: duas mensagens quase simultâneas do mesmo telefone. Quem criou primeiro vence;
			// só reconsulta o que já foi salvo em vez de propagar o conflito. Não é "recém-criado" por esta
			// chamada, mas a outra mensagem concorrente também não perguntou o nome ainda — trata como recém-criado
			// mesmo assim, para não arriscar interpretar esta mensagem como resposta a uma pergunta que não foi feita.
			return pacienteRepositoryGateway
					.buscarPorTelefone(telefone)
					.map(IdentificarPacienteOutput::deRecemCriado)
					.orElseThrow(() -> e);
		}
	}
}
