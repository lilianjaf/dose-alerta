package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;

public class CompletarCadastroUseCase {

	private final PacienteRepositoryGateway pacienteRepositoryGateway;
	private final CadastroSusGateway cadastroSusGateway;

	public CompletarCadastroUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway, CadastroSusGateway cadastroSusGateway) {
		this.pacienteRepositoryGateway = pacienteRepositoryGateway;
		this.cadastroSusGateway = cadastroSusGateway;
	}

	public Paciente executar(CompletarCadastroInput input) {
		Paciente existente = pacienteRepositoryGateway
				.buscarPorTelefone(input.telefone())
				.orElseThrow(() -> new PacienteNaoEncontradoException(input.telefone()));

		String nome = cadastroSusGateway
				.buscarNomePorNumeroInscricao(input.numeroInscricaoSus())
				.orElseThrow(() -> new NumeroInscricaoSusNaoEncontradoException(input.numeroInscricaoSus()));

		Paciente completo = Paciente.existente(
				existente.getId(), nome, existente.getTelefone(), existente.getSenhaHash(), existente.getCriadoEm());
		return pacienteRepositoryGateway.salvar(completo);
	}
}
