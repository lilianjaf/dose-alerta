package com.dosealerta.usuario.core.dto;

import jakarta.validation.constraints.NotBlank;

public record AutenticarPacienteInput(

		@NotBlank
		String telefone,

		@NotBlank
		String senha) {
}
