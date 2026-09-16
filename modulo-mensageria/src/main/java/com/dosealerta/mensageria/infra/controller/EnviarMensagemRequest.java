package com.dosealerta.mensageria.infra.controller;

import jakarta.validation.constraints.NotBlank;

record EnviarMensagemRequest(

		@NotBlank
		String telefone,

		@NotBlank
		String texto,

		@NotBlank
		String textoBotao) {
}
