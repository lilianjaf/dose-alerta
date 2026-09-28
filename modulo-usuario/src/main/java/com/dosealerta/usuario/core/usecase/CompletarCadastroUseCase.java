package com.dosealerta.usuario.core.usecase;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;

/**
 * Recebe o número de inscrição do SUS (Cartão SUS) respondido pelo paciente depois que
 * {@link IdentificarPacientePorTelefoneUseCase} não achou o telefone nem no cadastro nem no SUS. O nome nunca é
 * digitado pelo paciente — vem sempre do SUS, pela mesma consulta que já é feita para o telefone, só que agora
 * pela inscrição. Se o número informado não bate com nada no SUS, falha (o modulo-mensageria pede de novo em vez
 * de completar o cadastro com um nome não confirmado).
 */
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
