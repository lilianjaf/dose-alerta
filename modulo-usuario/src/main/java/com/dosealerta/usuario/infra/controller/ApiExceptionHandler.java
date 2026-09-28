package com.dosealerta.usuario.infra.controller;

import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, String>> tratar(MethodArgumentNotValidException e) {
		String detalhes = e.getBindingResult().getFieldErrors().stream()
				.map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", detalhes));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<Map<String, String>> tratar(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("mensagem", "Corpo da requisição ausente ou com JSON malformado"));
	}

	@ExceptionHandler(TelefoneJaCadastradoException.class)
	ResponseEntity<Map<String, String>> tratar(TelefoneJaCadastradoException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	ResponseEntity<Map<String, String>> tratar(CredenciaisInvalidasException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(PacienteNaoEncontradoException.class)
	ResponseEntity<Map<String, String>> tratar(PacienteNaoEncontradoException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(NumeroInscricaoSusNaoEncontradoException.class)
	ResponseEntity<Map<String, String>> tratar(NumeroInscricaoSusNaoEncontradoException e) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Map.of("mensagem", e.getMessage()));
	}
}
