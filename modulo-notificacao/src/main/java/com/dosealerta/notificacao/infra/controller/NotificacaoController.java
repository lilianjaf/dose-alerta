package com.dosealerta.notificacao.infra.controller;

import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.usecase.SolicitarEnvioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificacaoController {

	private final SolicitarEnvioUseCase solicitarEnvioUseCase;

	public NotificacaoController(SolicitarEnvioUseCase solicitarEnvioUseCase) {
		this.solicitarEnvioUseCase = solicitarEnvioUseCase;
	}

	@PostMapping("/notificacoes/solicitar-envio")
	public ResponseEntity<Void> solicitarEnvio(@Valid @RequestBody SolicitarEnvioInput input) {
		solicitarEnvioUseCase.executar(input);
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}
}
