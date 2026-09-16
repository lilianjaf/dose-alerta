package com.dosealerta.mensageria.infra.controller;

import jakarta.validation.constraints.NotBlank;

record RealizarLigacaoRequest(

		@NotBlank
		String telefone,

		@NotBlank
		String textoFalado) {
}
