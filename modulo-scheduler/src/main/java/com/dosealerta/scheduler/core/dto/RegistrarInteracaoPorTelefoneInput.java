package com.dosealerta.scheduler.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrarInteracaoPorTelefoneInput(

		@NotBlank
		@Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Telefone deve estar em formato E.164, ex: +5511999999999")
		String telefone) {
}
