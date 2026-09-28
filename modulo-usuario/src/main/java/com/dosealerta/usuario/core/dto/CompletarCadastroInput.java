package com.dosealerta.usuario.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CompletarCadastroInput(

		@NotBlank
		@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "telefone deve estar em formato E.164, ex: +5511999999999")
		String telefone,

		@NotBlank
		@Pattern(regexp = "^[0-9]{15}$", message = "numeroInscricaoSus deve ter 15 dígitos numéricos (Cartão SUS)")
		String numeroInscricaoSus) {
}
