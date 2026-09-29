package com.dosealerta.ia.infra.controller;

import com.dosealerta.ia.core.exception.AudioInvalidoException;
import com.dosealerta.ia.core.exception.CampoInformadoEmBrancoException;
import com.dosealerta.ia.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.ia.core.exception.DuracaoDiasForaDaFaixaException;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.FrequenciaHorasForaDaFaixaException;
import com.dosealerta.ia.core.exception.HorarioInicialObrigatorioException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaIdObrigatorioException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import com.dosealerta.ia.core.exception.ReceitaJaConfirmadaException;
import com.dosealerta.ia.core.exception.ReceitaNaoEncontradaException;
import com.dosealerta.ia.core.exception.TelefoneFormatoInvalidoException;
import com.dosealerta.ia.core.exception.TelefoneObrigatorioException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
	private static final String TIPO_BASE = "https://dosealerta.com/erros/";
	private static final String TIPO_VALIDACAO = "validacao";
	private static final String TIPO_CONFLITO = "conflito";
	private static final String TIPO_NAO_ENCONTRADO = "nao-encontrado";
	private static final String TIPO_REGRA_DE_NEGOCIO = "regra-de-negocio";
	private static final String TIPO_SERVICO_INDISPONIVEL = "servico-indisponivel";
	private static final String TIPO_ERRO_INTERNO = "erro-interno";
	private static final String TITULO_VALIDACAO = "Requisição inválida";
	private static final String TITULO_CONFLITO = "Conflito de dados";
	private static final String TITULO_NAO_ENCONTRADO = "Recurso não encontrado";
	private static final String TITULO_REGRA_DE_NEGOCIO = "Regra de negócio não atendida";
	private static final String TITULO_SERVICO_INDISPONIVEL = "Serviço indisponível";
	private static final String TITULO_ERRO_INTERNO = "Erro interno";
	private static final String DETALHE_CORPO_INVALIDO = "Corpo da requisição ausente ou com JSON malformado";
	private static final String DETALHE_PARAMETRO_AUSENTE = "Parâmetro obrigatório ausente: '%s'";
	private static final String DETALHE_PARAMETRO_INVALIDO = "Valor inválido para o parâmetro '%s'";
	private static final String DETALHE_PARTE_AUSENTE = "Parte obrigatória ausente no multipart: '%s'";
	private static final String DETALHE_MEDIA_TYPE = "Content-Type não suportado: %s";
	private static final String DETALHE_IMAGEM_GRANDE = "Imagem da receita excede o tamanho máximo de 10MB";
	private static final String DETALHE_ERRO_INTERNO = "Erro inesperado ao processar a requisição";
	private static final String MENSAGEM_LOG_ERRO_INTERNO = "Erro inesperado ao processar a requisição";
	private static final String SEPARADOR_CAMPOS = "; ";
	private static final String SEPARADOR_CAMPO_MENSAGEM = ": ";
	private static final String PROPRIEDADE_MOTIVO = "motivo";
	private static final String PROPRIEDADE_CAMPOS_PENDENTES = "camposPendentes";

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

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			MissingServletRequestParameterException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return requisicaoInvalida(e, DETALHE_PARAMETRO_AUSENTE.formatted(e.getParameterName()), headers, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(
			TypeMismatchException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return requisicaoInvalida(e, DETALHE_PARAMETRO_INVALIDO.formatted(e.getPropertyName()), headers, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart(
			MissingServletRequestPartException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return requisicaoInvalida(e, DETALHE_PARTE_AUSENTE.formatted(e.getRequestPartName()), headers, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return handleExceptionInternal(
				e,
				problema(HttpStatus.UNSUPPORTED_MEDIA_TYPE, TIPO_VALIDACAO, TITULO_VALIDACAO,
						DETALHE_MEDIA_TYPE.formatted(e.getContentType())),
				headers,
				HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				request);
	}

	@Override
	protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
			MaxUploadSizeExceededException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return handleExceptionInternal(
				e,
				problema(HttpStatus.PAYLOAD_TOO_LARGE, TIPO_VALIDACAO, TITULO_VALIDACAO, DETALHE_IMAGEM_GRANDE),
				headers,
				HttpStatus.PAYLOAD_TOO_LARGE,
				request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ProblemDetail> tratar(ConstraintViolationException e) {
		return resposta(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, e.getMessage());
	}

	@ExceptionHandler({
			ReceitaIdObrigatorioException.class,
			TelefoneObrigatorioException.class,
			PacienteIdObrigatorioException.class,
			HorarioInicialObrigatorioException.class,
			ImagemReceitaInvalidaException.class,
			AudioInvalidoException.class,
			TelefoneFormatoInvalidoException.class,
			CampoInformadoEmBrancoException.class,
			FrequenciaHorasForaDaFaixaException.class,
			DuracaoDiasForaDaFaixaException.class
	})
	ResponseEntity<ProblemDetail> tratarCampoObrigatorio(RuntimeException e) {
		return resposta(HttpStatus.BAD_REQUEST, TIPO_VALIDACAO, TITULO_VALIDACAO, e.getMessage());
	}

	@ExceptionHandler(ReceitaNaoEncontradaException.class)
	ResponseEntity<ProblemDetail> tratar(ReceitaNaoEncontradaException e) {
		return resposta(HttpStatus.NOT_FOUND, TIPO_NAO_ENCONTRADO, TITULO_NAO_ENCONTRADO, e.getMessage());
	}

	@ExceptionHandler(ReceitaJaConfirmadaException.class)
	ResponseEntity<ProblemDetail> tratar(ReceitaJaConfirmadaException e) {
		return resposta(HttpStatus.CONFLICT, TIPO_CONFLITO, TITULO_CONFLITO, e.getMessage());
	}

	@ExceptionHandler(ReceitaFormalNaoIdentificadaException.class)
	ResponseEntity<ProblemDetail> tratar(ReceitaFormalNaoIdentificadaException e) {
		ProblemDetail problema = problema(
				HttpStatus.UNPROCESSABLE_CONTENT, TIPO_REGRA_DE_NEGOCIO, TITULO_REGRA_DE_NEGOCIO, e.getMessage());
		problema.setProperty(PROPRIEDADE_MOTIVO, e.getMotivo());
		return ResponseEntity.unprocessableContent().body(problema);
	}

	@ExceptionHandler(DadosReceitaIncompletosException.class)
	ResponseEntity<ProblemDetail> tratar(DadosReceitaIncompletosException e) {
		ProblemDetail problema = problema(
				HttpStatus.UNPROCESSABLE_CONTENT, TIPO_REGRA_DE_NEGOCIO, TITULO_REGRA_DE_NEGOCIO, e.getMessage());
		problema.setProperty(PROPRIEDADE_CAMPOS_PENDENTES, e.getCamposPendentes());
		return ResponseEntity.unprocessableContent().body(problema);
	}

	@ExceptionHandler(ReceitaInvalidaException.class)
	ResponseEntity<ProblemDetail> tratar(ReceitaInvalidaException e) {
		return resposta(
				HttpStatus.UNPROCESSABLE_CONTENT, TIPO_REGRA_DE_NEGOCIO, TITULO_REGRA_DE_NEGOCIO, e.getMessage());
	}

	@ExceptionHandler(ExtracaoReceitaFalhouException.class)
	ResponseEntity<ProblemDetail> tratar(ExtracaoReceitaFalhouException e) {
		return resposta(
				HttpStatus.SERVICE_UNAVAILABLE, TIPO_SERVICO_INDISPONIVEL, TITULO_SERVICO_INDISPONIVEL, e.getMessage());
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
