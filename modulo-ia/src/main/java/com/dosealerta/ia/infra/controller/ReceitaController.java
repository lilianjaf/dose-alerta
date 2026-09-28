package com.dosealerta.ia.infra.controller;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.dto.ConfirmarReceitaPorTelefoneInput;
import com.dosealerta.ia.core.dto.ExtracaoOutput;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ReceitaOutput;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
public class ReceitaController {

	private static final long TAMANHO_MAXIMO_BYTES = 10L * 1024 * 1024;

	private final ExtrairReceitaUseCase extrairReceitaUseCase;
	private final ExtrairReceitaUseCase extrairReceitaUseCaseMock;
	private final ConfirmarReceitaUseCase confirmarReceitaUseCase;
	private final ConfirmarReceitaPorTelefoneUseCase confirmarReceitaPorTelefoneUseCase;
	private final BuscarReceitaUseCase buscarReceitaUseCase;

	public ReceitaController(
			@Qualifier("extrairReceitaUseCase") ExtrairReceitaUseCase extrairReceitaUseCase,
			@Qualifier("extrairReceitaUseCaseMock") ExtrairReceitaUseCase extrairReceitaUseCaseMock,
			ConfirmarReceitaUseCase confirmarReceitaUseCase,
			ConfirmarReceitaPorTelefoneUseCase confirmarReceitaPorTelefoneUseCase,
			BuscarReceitaUseCase buscarReceitaUseCase) {
		this.extrairReceitaUseCase = extrairReceitaUseCase;
		this.extrairReceitaUseCaseMock = extrairReceitaUseCaseMock;
		this.confirmarReceitaUseCase = confirmarReceitaUseCase;
		this.confirmarReceitaPorTelefoneUseCase = confirmarReceitaPorTelefoneUseCase;
		this.buscarReceitaUseCase = buscarReceitaUseCase;
	}

	@PostMapping(value = "/receitas/extrair", consumes = "multipart/form-data")
	public ResponseEntity<ExtracaoOutput> extrair(
			@RequestParam("imagem") MultipartFile imagem,
			@RequestParam @NotNull UUID pacienteId,
			@RequestParam @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Telefone deve estar em formato E.164, ex: +5511999999999")
					String telefone,
			@RequestParam @NotNull Instant horarioInicial) {
		return extrairCom(extrairReceitaUseCase, imagem, pacienteId, telefone, horarioInicial);
	}

	/**
	 * Mesmo contrato de {@link #extrair}, mas com dados fixos em vez de chamar o Gemini — pra testar confirmação
	 * e criação de alarme quando o modelo de visão estiver fora do ar ou sobrecarregado (ver
	 * {@code GeminiExtratorReceitaGateway}). A imagem enviada é ignorada.
	 */
	@PostMapping(value = "/receitas/extrair-mock", consumes = "multipart/form-data")
	public ResponseEntity<ExtracaoOutput> extrairMock(
			@RequestParam("imagem") MultipartFile imagem,
			@RequestParam @NotNull UUID pacienteId,
			@RequestParam @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Telefone deve estar em formato E.164, ex: +5511999999999")
					String telefone,
			@RequestParam @NotNull Instant horarioInicial) {
		return extrairCom(extrairReceitaUseCaseMock, imagem, pacienteId, telefone, horarioInicial);
	}

	private ResponseEntity<ExtracaoOutput> extrairCom(
			ExtrairReceitaUseCase useCase, MultipartFile imagem, UUID pacienteId, String telefone, Instant horarioInicial) {
		var input = new ExtrairReceitaInput(pacienteId, telefone, horarioInicial, lerBytes(validarImagem(imagem)));
		ExtracaoOutput output = ExtracaoOutput.de(useCase.executar(input));
		return ResponseEntity.status(HttpStatus.CREATED).body(output);
	}

	@PostMapping("/receitas/{id}/confirmar")
	@Transactional
	public ReceitaOutput confirmar(
			@PathVariable UUID id, @Valid @RequestBody(required = false) ConfirmarReceitaInput input) {
		ConfirmarReceitaInput correcoes = input != null ? input : ConfirmarReceitaInput.semCorrecoes();
		return ReceitaOutput.de(confirmarReceitaUseCase.executar(id, correcoes));
	}

	/** Chamado pelo modulo-mensageria quando o paciente responde uma mensagem de confirmação no WhatsApp. */
	@PostMapping("/receitas/confirmar-por-telefone")
	@Transactional
	public ReceitaOutput confirmarPorTelefone(@Valid @RequestBody ConfirmarReceitaPorTelefoneInput input) {
		Receita receita = confirmarReceitaPorTelefoneUseCase.executar(input.telefone(), input.paraCorrecoes());
		return ReceitaOutput.de(receita);
	}

	@GetMapping("/receitas/{id}")
	public ReceitaOutput buscar(@PathVariable UUID id) {
		return ReceitaOutput.de(buscarReceitaUseCase.executar(id));
	}

	private MultipartFile validarImagem(MultipartFile imagem) {
		if (imagem == null || imagem.isEmpty()) {
			throw new ImagemReceitaInvalidaException("Imagem da receita não pode ser vazia");
		}
		if (imagem.getSize() > TAMANHO_MAXIMO_BYTES) {
			throw new ImagemReceitaInvalidaException("Imagem da receita excede o tamanho máximo de 10MB");
		}
		return imagem;
	}

	private byte[] lerBytes(MultipartFile imagem) {
		try {
			return imagem.getBytes();
		} catch (IOException e) {
			throw new ImagemReceitaInvalidaException("Falha ao ler a imagem enviada");
		}
	}
}
