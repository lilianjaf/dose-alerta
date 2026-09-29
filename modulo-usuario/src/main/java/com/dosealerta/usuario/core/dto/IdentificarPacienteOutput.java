package com.dosealerta.usuario.core.dto;

import com.dosealerta.usuario.core.domain.Paciente;
import java.util.UUID;

public record IdentificarPacienteOutput(UUID pacienteId, String nome, boolean cadastroCompleto, boolean recemCriado) {

	public static IdentificarPacienteOutput deExistente(Paciente paciente) {
		return new IdentificarPacienteOutput(paciente.getId(), paciente.getNome(), !paciente.cadastroIncompleto(), false);
	}

	public static IdentificarPacienteOutput deRecemCriado(Paciente paciente) {
		return new IdentificarPacienteOutput(paciente.getId(), paciente.getNome(), !paciente.cadastroIncompleto(), true);
	}
}
