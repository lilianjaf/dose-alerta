package com.dosealerta.usuario.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastrarPacienteInput(

		@NotBlank
		String nome,

		@NotBlank
		@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "telefone deve estar em formato E.164, ex: +5511999999999")
		String telefone,

		@NotBlank
		@Size(min = 8, message = "senha deve ter no mínimo 8 caracteres")
		String senha) {
}
