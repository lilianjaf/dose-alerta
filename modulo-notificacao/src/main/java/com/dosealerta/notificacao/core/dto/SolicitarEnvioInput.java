package com.dosealerta.notificacao.core.dto;

import com.dosealerta.notificacao.core.domain.EtapaEscalonamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record SolicitarEnvioInput(

		@NotNull
		UUID alarmeId,

		@NotNull
		UUID pacienteId,

		@NotBlank
		@Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Telefone deve estar em formato E.164, ex: +5511999999999")
		String telefone,

		@NotBlank
		String medicamento,

		@NotBlank
		String dose,

		@NotNull
		EtapaEscalonamento etapa) {
}
