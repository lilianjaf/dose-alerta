package com.dosealerta.usuario.infra.gateway;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.infra.gateway.mapper.PacienteMapper;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
class PacienteRepositoryGatewayImpl implements PacienteRepositoryGateway {

	private final PacienteJpaRepository pacienteJpaRepository;

	PacienteRepositoryGatewayImpl(PacienteJpaRepository pacienteJpaRepository) {
		this.pacienteJpaRepository = pacienteJpaRepository;
	}

	@Override
	public Paciente salvar(Paciente paciente) {
		try {
			var entidade = pacienteJpaRepository.save(PacienteMapper.paraEntidade(paciente));
			return PacienteMapper.paraDominio(entidade);
		} catch (DataIntegrityViolationException e) {
			throw new TelefoneJaCadastradoException(paciente.getTelefone());
		}
	}

	@Override
	public Optional<Paciente> buscarPorTelefone(String telefone) {
		return pacienteJpaRepository.findByTelefone(telefone).map(PacienteMapper::paraDominio);
	}
}
