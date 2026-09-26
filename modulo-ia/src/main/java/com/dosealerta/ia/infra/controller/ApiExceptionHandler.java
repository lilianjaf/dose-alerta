package com.dosealerta.ia.infra.controller;

import com.dosealerta.ia.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import com.dosealerta.ia.core.exception.ReceitaJaConfirmadaException;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Map<String, String>> tratar(ConstraintViolationException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(ReceitaNaoEncontradaException.class)
	ResponseEntity<Map<String, String>> tratar(ReceitaNaoEncontradaException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(ReceitaJaConfirmadaException.class)
	ResponseEntity<Map<String, String>> tratar(ReceitaJaConfirmadaException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(ImagemReceitaInvalidaException.class)
	ResponseEntity<Map<String, String>> tratar(ImagemReceitaInvalidaException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(ReceitaFormalNaoIdentificadaException.class)
	ResponseEntity<Map<String, String>> tratar(ReceitaFormalNaoIdentificadaException e) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(Map.of("mensagem", e.getMessage(), "motivo", e.getMotivo()));
	}

	@ExceptionHandler(DadosReceitaIncompletosException.class)
	ResponseEntity<Map<String, Object>> tratar(DadosReceitaIncompletosException e) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(Map.of("mensagem", e.getMessage(), "camposPendentes", e.getCamposPendentes()));
	}

	@ExceptionHandler(ReceitaInvalidaException.class)
	ResponseEntity<Map<String, String>> tratar(ReceitaInvalidaException e) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(ExtracaoReceitaFalhouException.class)
	ResponseEntity<Map<String, String>> tratar(ExtracaoReceitaFalhouException e) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("mensagem", e.getMessage()));
	}
}
