package com.dosealerta.relatorioadesao.infra.controller;

import com.dosealerta.relatorioadesao.core.dto.RegistrarInteracaoInput;
import com.dosealerta.relatorioadesao.core.usecase.RegistrarInteracaoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ponto de entrada acionado pelo publisher assíncrono do outbox de interações do
 * modulo-scheduler (Etapa 8.1) — ver {@code InteracaoRegistradaEvent} em
 * CONTRATOS_EVENTOS.md.
 */
@RestController
class InteracaoController {

	private final RegistrarInteracaoUseCase registrarInteracaoUseCase;

	InteracaoController(RegistrarInteracaoUseCase registrarInteracaoUseCase) {
		this.registrarInteracaoUseCase = registrarInteracaoUseCase;
	}

	@PostMapping("/interacoes")
	ResponseEntity<Void> registrar(@Valid @RequestBody RegistrarInteracaoInput input) {
		registrarInteracaoUseCase.executar(input);
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}
}
