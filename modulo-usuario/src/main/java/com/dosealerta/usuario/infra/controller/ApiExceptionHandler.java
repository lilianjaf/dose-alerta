package com.dosealerta.usuario.infra.controller;

import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.exception.NomeObrigatorioException;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusFormatoInvalidoException;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusObrigatorioException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.exception.SenhaCurtaException;
import com.dosealerta.usuario.core.exception.SenhaObrigatoriaException;
import com.dosealerta.usuario.core.exception.TelefoneFormatoInvalidoException;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;
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
	private static final String TIPO_CONFLITO = "conflito";
	private static final String TIPO_NAO_AUTENTICADO = "nao-autenticado";
	private static final String TIPO_NAO_ENCONTRADO = "nao-encontrado";
	private static final String TIPO_REGRA_DE_NEGOCIO = "regra-de-negocio";
	private static final String TIPO_ERRO_INTERNO = "erro-interno";
	private static final String TITULO_VALIDACAO = "Requisição inválida";
	private static final String TITULO_CONFLITO = "Conflito de dados";
	private static final String TITULO_NAO_AUTENTICADO = "Não autenticado";
	private static final String TITULO_NAO_ENCONTRADO = "Recurso não encontrado";
	private static final String TITULO_REGRA_DE_NEGOCIO = "Regra de negócio não atendida";
	private static final String TITULO_ERRO_INTERNO = "Erro interno";
	private static final String DETALHE_CORPO_INVALIDO = "Corpo da requisição ausente ou com JSON malformado";
	private static final String DETALHE_ERRO_INTERNO = "Erro inesperado ao processar a requisição";
	private static final String SEPARADOR_CAMPOS = "; ";
	private static final String SEPARADOR_CAMPO_MENSAGEM = ": ";
	private static final String MENSAGEM_LOG_ERRO_INTERNO = "Erro inesperado ao processar a requisição";

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detalhes = e.getBindingResult().getFieldErrors().stream()
				.map(erro -> erro.getField() + SEPARADOR_CAMPO_MENSAGEM + erro.getDefaultMessage())
				.collect(Collectors.joining(SEPARADOR_CAMPOS));
		return handleExceptionInternal(
				e, problema(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, detalhes), headers,
				HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return handleExceptionInternal(
				e, problema(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, DETALHE_CORPO_INVALIDO), headers,
				HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler({
			NomeObrigatorioException.class,
			TelefoneObrigatorioException.class,
			SenhaObrigatoriaException.class,
			NumeroInscricaoSusObrigatorioException.class,
			TelefoneFormatoInvalidoException.class,
			SenhaCurtaException.class,
			NumeroInscricaoSusFormatoInvalidoException.class
	})
	ResponseEntity<ProblemDetail> tratarCampoObrigatorio(RuntimeException e) {
		return resposta(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, e.getMessage());
	}

	@ExceptionHandler(TelefoneJaCadastradoException.class)
	ResponseEntity<ProblemDetail> tratar(TelefoneJaCadastradoException e) {
		return resposta(HttpStatus.CONFLICT, TIPO_CONFLITO, TITULO_CONFLITO, e.getMessage());
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	ResponseEntity<ProblemDetail> tratar(CredenciaisInvalidasException e) {
		return resposta(HttpStatus.UNAUTHORIZED, TIPO_NAO_AUTENTICADO, TITULO_NAO_AUTENTICADO, e.getMessage());
	}

	@ExceptionHandler(PacienteNaoEncontradoException.class)
	ResponseEntity<ProblemDetail> tratar(PacienteNaoEncontradoException e) {
		return resposta(HttpStatus.NOT_FOUND, TIPO_NAO_ENCONTRADO, TITULO_NAO_ENCONTRADO, e.getMessage());
	}

	@ExceptionHandler(NumeroInscricaoSusNaoEncontradoException.class)
	ResponseEntity<ProblemDetail> tratar(NumeroInscricaoSusNaoEncontradoException e) {
		return resposta(
				HttpStatus.UNPROCESSABLE_CONTENT, TIPO_REGRA_DE_NEGOCIO, TITULO_REGRA_DE_NEGOCIO, e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> tratarInesperado(Exception e) {
		log.error(MENSAGEM_LOG_ERRO_INTERNO, e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, TIPO_ERRO_INTERNO, TITULO_ERRO_INTERNO, DETALHE_ERRO_INTERNO);
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
