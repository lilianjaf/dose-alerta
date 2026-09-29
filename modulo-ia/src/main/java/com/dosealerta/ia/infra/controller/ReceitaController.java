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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	private static final String MENSAGEM_FALHA_LEITURA_IMAGEM = "Falha ao ler a imagem enviada";

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
		var input = new ExtrairReceitaInput(pacienteId, telefone, horarioInicial, lerBytes(imagem));
		ExtracaoOutput output = ExtracaoOutput.de(useCase.executar(input));
		return ResponseEntity.status(HttpStatus.CREATED).body(output);
	}

	@PostMapping("/receitas/{id}/confirmar")
	public ReceitaOutput confirmar(
			@PathVariable UUID id, @RequestBody(required = false) ConfirmarReceitaInput input) {
		ConfirmarReceitaInput correcoes = input != null ? input : ConfirmarReceitaInput.semCorrecoes();
		return ReceitaOutput.de(confirmarReceitaUseCase.executar(id, correcoes));
	}

	@PostMapping("/receitas/confirmar-por-telefone")
	public ReceitaOutput confirmarPorTelefone(@RequestBody ConfirmarReceitaPorTelefoneInput input) {
		Receita receita = confirmarReceitaPorTelefoneUseCase.executar(input.telefone(), input.paraCorrecoes());
		return ReceitaOutput.de(receita);
	}

	@GetMapping("/receitas/{id}")
	public ReceitaOutput buscar(@PathVariable UUID id) {
		return ReceitaOutput.de(buscarReceitaUseCase.executar(id));
	}

	private byte[] lerBytes(MultipartFile imagem) {
		try {
			return imagem.getBytes();
		} catch (IOException e) {
			throw new ImagemReceitaInvalidaException(MENSAGEM_FALHA_LEITURA_IMAGEM);
		}
	}
}
