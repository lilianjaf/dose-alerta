package com.dosealerta.usuario.core.dto;

import com.dosealerta.usuario.core.domain.Paciente;
import java.time.Instant;
import java.util.UUID;

public record PacienteOutput(UUID id, String nome, String telefone, Instant criadoEm) {

	public static PacienteOutput de(Paciente paciente) {
		return new PacienteOutput(paciente.getId(), paciente.getNome(), paciente.getTelefone(), paciente.getCriadoEm());
	}
}
