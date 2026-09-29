package com.dosealerta.mensageria.infra.controller;

import com.dosealerta.mensageria.core.exception.EnvioMensagemFalhouException;
import com.dosealerta.mensageria.core.exception.LigacaoFalhouException;
import com.dosealerta.mensageria.core.exception.TelefoneObrigatorioException;
import com.dosealerta.mensageria.infra.twilio.DetalheErroTwilio;
import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
	private static final String TIPO_BASE = "https://dosealerta.com/erros/";
	private static final String TIPO_VALIDACAO = "validacao";
	private static final String TIPO_FALHA_PROVEDOR = "falha-provedor";
	private static final String TIPO_ERRO_INTERNO = "erro-interno";
	private static final String TITULO_VALIDACAO = "Requisição inválida";
	private static final String TITULO_FALHA_PROVEDOR = "Falha no provedor de mensageria";
	private static final String TITULO_ERRO_INTERNO = "Erro interno";
	private static final String DETALHE_CORPO_INVALIDO = "Corpo da requisição ausente ou com JSON malformado";
	private static final String DETALHE_ERRO_INTERNO = "Erro inesperado ao processar a requisição";
	private static final String MENSAGEM_LOG_ERRO_INTERNO = "Erro inesperado ao processar a requisição";
	private static final String PROPRIEDADE_MOTIVO = "motivo";
	private static final String SEPARADOR_CAMPOS = "; ";
	private static final String SEPARADOR_CAMPO_MENSAGEM = ": ";

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detalhes = e.getBindingResult().getFieldErrors().stream()
				.map(erro -> erro.getField() + SEPARADOR_CAMPO_MENSAGEM + erro.getDefaultMessage())
				.collect(Collectors.joining(SEPARADOR_CAMPOS));
		return requisicaoInvalida(e, detalhes, headers, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return requisicaoInvalida(e, DETALHE_CORPO_INVALIDO, headers, request);
	}

	@ExceptionHandler({EnvioMensagemFalhouException.class, LigacaoFalhouException.class})
	ResponseEntity<ProblemDetail> tratarFalhaTwilio(RuntimeException e) {
		ProblemDetail problema =
				problema(HttpStatus.BAD_GATEWAY, TIPO_FALHA_PROVEDOR, TITULO_FALHA_PROVEDOR, e.getMessage());
		problema.setProperty(PROPRIEDADE_MOTIVO, DetalheErroTwilio.descrever(e));
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problema);
	}

	@ExceptionHandler({IllegalArgumentException.class, TelefoneObrigatorioException.class})
	ResponseEntity<ProblemDetail> tratarEntradaInvalida(RuntimeException e) {
		return resposta(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> tratarInesperado(Exception e) {
		log.error(MENSAGEM_LOG_ERRO_INTERNO, e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, TIPO_ERRO_INTERNO, TITULO_ERRO_INTERNO, DETALHE_ERRO_INTERNO);
	}

	private ResponseEntity<Object> requisicaoInvalida(
			Exception e, String detalhe, HttpHeaders headers, WebRequest request) {
		return handleExceptionInternal(
				e, problema(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, detalhe), headers,
				HttpStatus.BAD_REQUEST, request);
	}

	private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String tipo, String titulo, String detalhe) {
		return ResponseEntity.status(status).body(problema(status, tipo, titulo, detalhe));
	}

	private ProblemDetail problema(HttpStatus status, String tipo, String titulo, String detalhe) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
		problema.setType(URI.create(TIPO_BASE + tipo));
		problema.setTitle(titulo);
		return problema;
	}
}
