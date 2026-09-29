package com.dosealerta.susmock;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
	private static final String TIPO_BASE = "https://dosealerta.com/erros/";
	private static final String TIPO_NAO_ENCONTRADO = "nao-encontrado";
	private static final String TIPO_ERRO_INTERNO = "erro-interno";
	private static final String TITULO_NAO_ENCONTRADO = "Recurso não encontrado";
	private static final String TITULO_ERRO_INTERNO = "Erro interno";
	private static final String DETALHE_ERRO_INTERNO = "Erro inesperado ao processar a requisição";
	private static final String MENSAGEM_LOG_ERRO_INTERNO = "Erro inesperado ao processar a requisição";

	@ExceptionHandler(CadastroSusNaoEncontradoException.class)
	ResponseEntity<ProblemDetail> tratarNaoEncontrado(CadastroSusNaoEncontradoException e) {
		return resposta(HttpStatus.NOT_FOUND, TIPO_NAO_ENCONTRADO, TITULO_NAO_ENCONTRADO, e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> tratarInesperado(Exception e) {
		log.error(MENSAGEM_LOG_ERRO_INTERNO, e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, TIPO_ERRO_INTERNO, TITULO_ERRO_INTERNO, DETALHE_ERRO_INTERNO);
	}

	private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String tipo, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setType(URI.create(TIPO_BASE + tipo));
		problema.setTitle(titulo);
		return ResponseEntity.status(status).body(problema);
	}
}
