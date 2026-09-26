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
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Map<String, String>> tratar(ConstraintViolationException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensagem", e.getMessage()));
	}

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

	@ExceptionHandler(MissingServletRequestParameterException.class)
	ResponseEntity<Map<String, String>> tratar(MissingServletRequestParameterException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("mensagem", "Parâmetro obrigatório ausente: '" + e.getParameterName() + "'"));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<Map<String, String>> tratar(MethodArgumentTypeMismatchException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("mensagem", "Valor inválido para o parâmetro '" + e.getName() + "'"));
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	ResponseEntity<Map<String, String>> tratar(MissingServletRequestPartException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("mensagem", "Parte obrigatória ausente no multipart: '" + e.getRequestPartName() + "'"));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	ResponseEntity<Map<String, String>> tratar(HttpMediaTypeNotSupportedException e) {
		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
				.body(Map.of("mensagem", "Content-Type não suportado: " + e.getContentType()));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ResponseEntity<Map<String, String>> tratar(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(Map.of("mensagem", "Imagem da receita excede o tamanho máximo de 10MB"));
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
