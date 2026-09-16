package com.dosealerta.mensageria.infra.controller;

import com.dosealerta.mensageria.core.exception.EnvioMensagemFalhouException;
import com.dosealerta.mensageria.core.exception.LigacaoFalhouException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler({EnvioMensagemFalhouException.class, LigacaoFalhouException.class})
	ResponseEntity<Map<String, String>> tratarFalhaTwilio(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<Map<String, String>> tratarEntradaInvalida(IllegalArgumentException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	ResponseEntity<Map<String, String>> tratarErroInterno(IllegalStateException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("mensagem", e.getMessage()));
	}
}
