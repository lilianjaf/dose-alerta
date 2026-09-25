package com.dosealerta.ia.infra.controller;

import com.dosealerta.ia.core.dto.ConfirmarReceitaInput;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ReceitaOutput;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
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
	private final ConfirmarReceitaUseCase confirmarReceitaUseCase;
	private final BuscarReceitaUseCase buscarReceitaUseCase;

	public ReceitaController(
			ExtrairReceitaUseCase extrairReceitaUseCase,
			ConfirmarReceitaUseCase confirmarReceitaUseCase,
			BuscarReceitaUseCase buscarReceitaUseCase) {
		this.extrairReceitaUseCase = extrairReceitaUseCase;
		this.confirmarReceitaUseCase = confirmarReceitaUseCase;
		this.buscarReceitaUseCase = buscarReceitaUseCase;
	}

	@PostMapping(value = "/receitas/extrair", consumes = "multipart/form-data")
	public ResponseEntity<ReceitaOutput> extrair(
			@RequestParam("imagem") MultipartFile imagem,
			@RequestParam @NotNull UUID pacienteId,
			@RequestParam @Pattern(regexp = "^\\+[0-9]{10,15}$", message = "Telefone deve estar em formato E.164, ex: +5511999999999")
					String telefone,
			@RequestParam @NotNull Instant horarioInicial) {
		var input = new ExtrairReceitaInput(pacienteId, telefone, horarioInicial, lerBytes(validarImagem(imagem)));
		ReceitaOutput output = ReceitaOutput.de(extrairReceitaUseCase.executar(input));
		return ResponseEntity.status(HttpStatus.CREATED).body(output);
	}

	@PostMapping("/receitas/{id}/confirmar")
	@Transactional
	public ReceitaOutput confirmar(@PathVariable UUID id, @Valid @RequestBody ConfirmarReceitaInput input) {
		return ReceitaOutput.de(confirmarReceitaUseCase.executar(id, input));
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
