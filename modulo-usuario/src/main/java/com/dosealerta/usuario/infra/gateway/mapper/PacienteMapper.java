package com.dosealerta.usuario.infra.gateway.mapper;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.infra.gateway.entity.PacienteJpaEntity;

public final class PacienteMapper {

	private PacienteMapper() {
	}

	public static PacienteJpaEntity paraEntidade(Paciente paciente) {
		return new PacienteJpaEntity(
				paciente.getId(),
				paciente.getNome(),
				paciente.getTelefone(),
				paciente.getSenhaHash(),
				paciente.getCriadoEm());
	}

	public static Paciente paraDominio(PacienteJpaEntity entity) {
		return Paciente.existente(
				entity.getId(),
				entity.getNome(),
				entity.getTelefone(),
				entity.getSenhaHash(),
				entity.getCriadoEm());
	}
}
