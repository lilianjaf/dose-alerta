package com.dosealerta.scheduler.infra.controller;

import com.dosealerta.scheduler.core.exception.AlarmeNaoEncontradoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(AlarmeNaoEncontradoException.class)
	ResponseEntity<Map<String, String>> tratar(AlarmeNaoEncontradoException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", e.getMessage()));
	}
}
