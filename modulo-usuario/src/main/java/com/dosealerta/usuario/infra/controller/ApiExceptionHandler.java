package com.dosealerta.usuario.infra.controller;

import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(TelefoneJaCadastradoException.class)
	ResponseEntity<Map<String, String>> tratar(TelefoneJaCadastradoException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	ResponseEntity<Map<String, String>> tratar(CredenciaisInvalidasException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", e.getMessage()));
	}
}
